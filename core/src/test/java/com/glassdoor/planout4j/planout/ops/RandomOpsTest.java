package com.glassdoor.planout4j.planout.ops;

import java.util.*;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Test;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import com.glassdoor.planout4j.planout.Assignment;
import com.glassdoor.planout4j.planout.ops.random.*;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.junit.Assert.*;

public class RandomOpsTest {

    // z_{\alpha/2} for \alpha=0.001, e.g., 99.9% CI: qnorm(1-(0.001/2))
    private static final double Z = 3.29;

    // number of trial runs
    private static final int N = 1000;

    // convert value_mass map to a density
    private Map<Object, Double> valueMassToDensity(Map<?, ? extends Number> valueMass) {
        Map<Object, Double> valueDensity = new LinkedHashMap<>();
        double sum = 0;
        for (Number mass : valueMass.values()) {
            sum += mass.doubleValue();
        }
        for (Object value : valueMass.keySet()) {
            valueDensity.put(value, valueMass.get(value).doubleValue() / sum);
        }
        return valueDensity;
    }

    // Make sure an experiment object generates the desired frequencies
    private void distributionTester(RandomFunctionBuilder builder, Map<?, ? extends Number> valueMass) {
        // run N trials of op with input i
        List<Object> xs = new ArrayList<>(N);
        for (int i=0; i < N; i++) {
            Assignment a = new Assignment(builder.experimentSalt, null);
            a.set("x", builder.getRandom(i));
            xs.add(a.get("x"));
        }
        Map<Object, Double> valueDensity = valueMassToDensity(valueMass);
        List<List<Object>> xsList;
        // handle sample test specially, each trial outcome is a list
        if (builder.randomOpClass == Sample.class) {
            // convert list of size N of samples to 'draws' lists of size N (aka "zip")
            // they all should have the same distribution density
            xsList = new ArrayList<>();
            for (Object x : xs) {
                int i=0;
                for (Object value : (List)x) {
                    if (i == xsList.size()) {
                        xsList.add(new ArrayList<>(N));
                    }
                    xsList.get(i++).add(value);
                }
            }
        } else {
            // just wrap the list
            xsList = ImmutableList.of(xs);
        }
        // test outcome frequencies against expected density
        for (List<Object> l : xsList) {
            assertProbs(l, valueDensity);
        }
    }

    // Assert a list of values has the same density as value_density
    private void assertProbs(List xs, Map<?, Double> valueDensity) {
        Map<Object, MutableInt> hist = new HashMap<>(xs.size());
        // count occurrences of each trial result
        for (Object value : xs) {
            MutableInt count = hist.get(value);
            if (count == null) {
                count = new MutableInt();
                hist.put(value, count);
            }
            count.increment();
        }
        // do binomial test of proportions for each item
        for (Object i : hist.keySet()) {
            assertProp(hist.get(i).doubleValue() / N, valueDensity.get(i));
        }
    }

    // Does a test of proportions
    private void assertProp(double observedP, double expectedP) {
        // normal approximation of binomial CI.
        // this should be OK for large N and values of p not too close to 0 or 1.
        double se = Z * sqrt(expectedP * (1 - expectedP) / N);
        assertTrue(abs(observedP - expectedP) <= se);
    }

    @Test
    public void testSalts() {
        int i = 20;
        Assignment a = new Assignment("assign_salt_a", null);

        // assigning variables with different names and the same unit should yield
        // different randomizations, when salts are not explicitly specified
        a.set("x", new RandomInteger(0, 100000, i));
        a.set("y", new RandomInteger(0, 100000, i));
        assertTrue(!a.get("x").equals(a.get("y")));

        // when salts are specified, they act the same way auto-salting does
        a.set("z", new RandomInteger(0, 100000, i).setSalt("x"));
        assertTrue(a.get("x").equals(a.get("z")));

        // when the Assignment-level salt is different, variables with the same
        // name (or salt) should generally be assigned to different values
        Assignment b = new Assignment("assign_salt_b", null);
        b.set("x", new RandomInteger(0, 100000, i));
        assertTrue(!a.get("x").equals(b.get("x")));

        // when a full salt is specified, only the full salt is used to do hashing
        a.set("f", new RandomInteger(0, 100000, i).setFullSalt("fs"));
        b.set("g", new RandomInteger(0, 100000, i).setFullSalt("fs"));
        assertTrue(a.get("f").equals(b.get("g")));
        a.set("f", new RandomInteger(0, 100000, i).setFullSalt("fs2"));
        b.set("f", new RandomInteger(0, 100000, i).setFullSalt("fs2"));
        assertTrue(a.get("f").equals(b.get("f")));
    }

    @Test
    public void testBernoulli() {
        double p = 0.0;
        distributionTester(new RandomFunctionBuilder(p, BernoulliTrial.class, ImmutableMap.of("p", p)),
                ImmutableMap.of(false, 1.0, true, 0.0));
        p = 0.1;
        distributionTester(new RandomFunctionBuilder(p, BernoulliTrial.class, ImmutableMap.of("p", p)),
                ImmutableMap.of(false, 0.9, true, 0.1));
        p = 1.0;
        distributionTester(new RandomFunctionBuilder(p, BernoulliTrial.class, ImmutableMap.of("p", p)),
                ImmutableMap.of(false, 0.0, true, 1.0));
        p = 0.5;
        distributionTester(new RandomFunctionBuilder(p, BernoulliTrial.class, ImmutableMap.of("p", p)),
                ImmutableMap.of(false, 0.5, true, 0.5));
    }

    @Test
    public void testUniformChoice() {
        List<?> c = ImmutableList.of("a");
        distributionTester(new RandomFunctionBuilder(c, UniformChoice.class, ImmutableMap.of("choices", c)),
                ImmutableMap.of("a", 1));
        c = ImmutableList.of("a", "b");
        distributionTester(new RandomFunctionBuilder(c, UniformChoice.class, ImmutableMap.of("choices", c)),
                ImmutableMap.of("a", 1, "b", 1));
        c = ImmutableList.of(1, 2, 3, 4);
        distributionTester(new RandomFunctionBuilder(c, UniformChoice.class, ImmutableMap.of("choices", c)),
                ImmutableMap.of(1, 1, 2, 1, 3, 1, 4, 1));
    }

    @Test
    public void testWeightedChoice() {
        Map<String, ? extends Number> d = ImmutableMap.of("a", 1);
        distributionTester(new RandomFunctionBuilder(d.values(), WeightedChoice.class,
                ImmutableMap.of("choices", new ArrayList<>(d.keySet()), "weights", new ArrayList<>(d.values()))), d);
        d = ImmutableMap.of("a", 1, "b", 2);
        distributionTester(new RandomFunctionBuilder(d.values(), WeightedChoice.class,
                ImmutableMap.of("choices", new ArrayList<>(d.keySet()), "weights", new ArrayList<>(d.values()))), d);
        d = ImmutableMap.of("a", 0, "b", 2, "c", 0);
        distributionTester(new RandomFunctionBuilder(d.values(), WeightedChoice.class,
                ImmutableMap.of("choices", new ArrayList<>(d.keySet()), "weights", new ArrayList<>(d.values()))), d);
        // we should be able to repeat the same choice multiple times in weightedChoice(). in this case we repeat 'a'.
        Multimap<String, ? extends Number> da = ImmutableMultimap.of("a", 1, "b", 2, "c", 0, "a", 2);
        Map<String, ? extends Number> db = ImmutableMap.of("a", 3, "b", 2, "c", 0);
        distributionTester(new RandomFunctionBuilder(da.values(), WeightedChoice.class,
                ImmutableMap.of("choices", new ArrayList<>(da.keys()), "weights", new ArrayList<>(da.values()))), db);
    }

    @Test
    public void testSample() {
        List<?> c = ImmutableList.of(1, 2, 3);
        distributionTester(new RandomFunctionBuilder(c, Sample.class, ImmutableMap.of("choices", c, "draws", 3)),
                ImmutableMap.of(1, 1, 2, 1, 3, 1));
        distributionTester(new RandomFunctionBuilder(c, Sample.class, ImmutableMap.of("choices", c, "draws", 2)),
                ImmutableMap.of(1, 1, 2, 1, 3, 1));
        c = ImmutableList.of("a", "a", "b");
        distributionTester(new RandomFunctionBuilder(c, Sample.class, ImmutableMap.of("choices", c, "draws", 3)),
                ImmutableMap.of("a", 2, "b", 1));
    }


    @Test
    public void testDistributionsConsistency() {
        String fullSalt = "some long value";
        for (int i=0; i < 6; i++) {
            String unit = UUID.randomUUID().toString();
            WeightedChoice<Integer> weightedChoice = new WeightedChoice<>(ImmutableList.of(10, 20), ImmutableList.of(0.25, 0.75), unit);
            weightedChoice.setFullSalt(fullSalt);
            int choice = weightedChoice.eval();
            WeightedChoice<Integer> weightedChoice2 = new WeightedChoice<>(ImmutableList.of(1, 2, 3, 4), ImmutableList.of(0.25, 0.25, 0.25, 0.25), unit);
            weightedChoice2.setFullSalt(fullSalt);
            int choice2 = weightedChoice2.eval();
            assertTrue(choice == 10 && choice2 == 1 || choice == 20 && choice2 > 1);
        }
    }

}
