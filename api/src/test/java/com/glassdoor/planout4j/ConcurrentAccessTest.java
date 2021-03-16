package com.glassdoor.planout4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;

import com.glassdoor.planout4j.config.Planout4jTestConfigHelper;
import com.glassdoor.planout4j.spring.Planout4jAppContext;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Planout4jAppContext.class)
public class ConcurrentAccessTest {

    @BeforeClass
    public static void before() {
        Planout4jTestConfigHelper.setSystemProperties(false);
    }

    @Resource
    private NamespaceFactory namespaceFactory;

    @Test
    public void testConcurrentAccess() throws Exception {
        // read test parameters
        final String namespaceName = System.getProperty("planout4j.ConcurrentAccessTest.namespace", "ns_test1");
        final String paramName = System.getProperty("planout4j.ConcurrentAccessTest.parameter", "srJobsResultsPerPage");
        final int threadsCnt = Integer.parseInt(System.getProperty("planout4j.ConcurrentAccessTest.threads", "100"));
        final int requestsCnt = Integer.parseInt(System.getProperty("planout4j.ConcurrentAccessTest.requests", "1000000"));
        final int totalUsers = Integer.parseInt(System.getProperty("planout4j.ConcurrentAccessTest.users", "10000"));
        final int totalFirstParts = Integer.parseInt(System.getProperty("planout4j.ConcurrentAccessTest.firstParts", "1000"));

        // initialize
        System.out.printf("*** Profiling parameter %s of namespace %s\n", paramName, namespaceName);
        assertNotNull(namespaceFactory.getNamespace(namespaceName, ImmutableMap.of("user_guid", "")).get());
        final RandomData random = createRandom();
        final ExecutorService threadPool = Executors.newFixedThreadPool(threadsCnt);
        final SetMultimap<String, String> resultsByUser = HashMultimap.create(totalUsers, 1);
        final Map<Object, MutableInt> paramValueFreq = new TreeMap<>();
        final Map<String, MutableInt> experimentFreq = new TreeMap<>();
        final List<String> guids = generateUserGUIDs(random, totalUsers, totalFirstParts);
        System.out.printf("*** Generated %s GUIDs with %s unique first parts\n", totalUsers, totalFirstParts);
        // log4j.properties is configured for DEBUG which will spit out too many messages in this case
        Logger.getLogger("com.glassdoor").setLevel(Level.INFO);

        // main loop
        final Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i=0; i < requestsCnt; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    final String userGuid = guids.get(random.nextInt(0, totalUsers - 1));
                    MDC.put("user_guid", userGuid);
                    final Namespace ns = namespaceFactory.getNamespace(namespaceName, ImmutableMap.of("user_guid", userGuid, "page_guid", "")).get();
                    final Object paramValue = ns.getParams().get(paramName);
                    final String experimentName = ns.getExperiment() == null ? "default" : ns.getExperiment().name;
                    synchronized (ConcurrentAccessTest.this) {
                        resultsByUser.put(userGuid, String.format("%s@%s:%s=%s", ns.getName(), experimentName, paramName, paramValue));
                        incKey(paramValueFreq, paramValue);
                        incKey(experimentFreq, experimentName);
                    }
                }
            });
        }
        threadPool.shutdown();
        final int runDurationLimitMins = 2;
        boolean normalCompletion = true;
        if (!threadPool.awaitTermination(runDurationLimitMins, TimeUnit.MINUTES)) {
            threadPool.shutdownNow();
            System.out.printf("*** ERROR: Test did not complete in %s minutes and was aborted", runDurationLimitMins);
            normalCompletion = false;
        }

        // verify and report
        Logger.getLogger("com.glassdoor").setLevel(Level.DEBUG);
        System.out.printf("*** Completed %s iterations using %s threads in %s\n", requestsCnt, threadsCnt, stopwatch.stop());
        boolean foundOscillators = false;
        for (String userGuid : resultsByUser.asMap().keySet()) {
            Collection<String> resultByUser = resultsByUser.get(userGuid);
            if (resultByUser.size() > 1) {
                foundOscillators = true;
                System.out.printf("*** ERROR: %s is an oscillator: %s\n", userGuid, resultByUser);
            }
        }
        dumpFrequencies(experimentFreq, "Experiments", requestsCnt);
        dumpFrequencies(paramValueFreq, "Parameter values", requestsCnt);
        assertFalse("Found oscillators!", foundOscillators);
        assertTrue("Test did not complete in time and was aborted", normalCompletion);
    }

    /**
     * To be able to use a specific seed and make everything reproducible.
     * @return RandomData
     */
    private RandomData createRandom() {
        final JDKRandomGenerator randomGen = new JDKRandomGenerator();
        randomGen.setSeed(1234567890);
        return new RandomDataImpl(randomGen);
    }

    /**
     * @return random hex string of exactly 16 chars
     */
    private String randomHexString(final RandomData random) {
        return Long.toHexString(random.nextLong((long)(Long.MAX_VALUE >> 3) + 1, Long.MAX_VALUE)).toUpperCase();
    }

    private List<String> generateUserGUIDs(final RandomData random, final int totalGUIDs, final int totalUniqueFirstParts) {
        Set<String> firstParts = new HashSet<>(totalUniqueFirstParts, 1);
        while (firstParts.size() < totalUniqueFirstParts) {
            firstParts.add(randomHexString(random));
        }
        List<String> firstPartsList = new ArrayList<>(firstParts);
        Set<String> guids = new HashSet<>(totalGUIDs, 1);
        while (guids.size() < totalGUIDs) {
            guids.add(firstPartsList.get(random.nextInt(0, totalUniqueFirstParts - 1)) + randomHexString(random));
        }
        return new ArrayList<>(guids);
    }

    private <K> void incKey(final Map<K, MutableInt> map, final K key) {
        MutableInt mint = map.get(key);
        if (mint == null) {
            mint = new MutableInt();
            map.put(key, mint);
        }
        mint.increment();
    }

    private void dumpFrequencies(final Map<?, MutableInt> map, final String what, final int requestsCnt) {
        final BigDecimal reqs = new BigDecimal(requestsCnt), hundred = new BigDecimal(100);
        System.out.printf("*** %s frequencies:\n", what);
        for (Object key : map.keySet()) {
            System.out.printf("\t%s\t:%s\n", key,
                    new BigDecimal(map.get(key).longValue()).multiply(hundred).divide(reqs, 1, BigDecimal.ROUND_HALF_EVEN));
        }
    }

}
