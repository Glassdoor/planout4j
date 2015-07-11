package com.glassdoor.planout4j.tools;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.argparse4j.inf.*;
import net.sourceforge.argparse4j.inf.Namespace;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import com.glassdoor.planout4j.*;
import com.glassdoor.planout4j.compiler.PlanoutDSLCompiler;
import com.glassdoor.planout4j.config.ValidationException;
import com.glassdoor.planout4j.planout.Interpreter;
import com.glassdoor.planout4j.util.Helper;

/**
 * Command-line interface for conducting performance test of resolving parameters of a given namespace.
 */
public class PerfTool {

    private static final Logger LOG = LoggerFactory.getLogger(PerfTool.class);

    public static void configureArgsParser(final Subparsers subparsers) {
        final Subparser perf = subparsers.addParser("perf").help("runs performance test");
        Planout4jTool.addBackendArgs(perf, false);
        perf.addArgument("-n", "--name").required(true).help("namespace name");
        final MutuallyExclusiveGroup experimentOrDefinition = perf.addMutuallyExclusiveGroup("additional selectors within namespace");
        experimentOrDefinition.addArgument("--exp").help("experiment name (requires --name NAME)");
        experimentOrDefinition.addArgument("--def").help("experiment definition key (requires --name NAME)");
        perf.addArgument("--iterations").type(Integer.class).setDefault(100000).help("number of test runs");
    }

    public static void execute(final Namespace parsedArgs) throws IOException, ValidationException {
        final String name = parsedArgs.getString("name");
        final NamespaceFactory nsFact = new SimpleNamespaceFactory();
        final Optional<NamespaceConfig> nsConf = nsFact.getNamespaceConfig(name);
        if (nsConf.isPresent()) {
            Experiment exp = null;
            final String expStr = parsedArgs.getString("exp"), defStr = parsedArgs.getString("def");
            if (expStr != null) {
                exp = nsConf.get().getActiveExperiment(expStr);
                if (exp == null) {
                    LOG.error("No active experiment named '{}' exists in namespace '{}'. All active experiments: {}",
                            expStr, name, nsConf.get().getActiveExperimentNames());
                    return;
                }
            } else if (defStr != null) {
                final ExperimentConfig expConf = nsConf.get().getExperimentConfig(defStr);
                if (expConf == null) {
                    LOG.error("No experiment definition named '{}' exists in namespace '{}'. All experiment definitions: {}",
                            defStr, name, nsConf.get().getExperimentConfigNames());
                    return;
                } else {
                    exp = new Experiment(defStr,
                            MoreObjects.firstNonNull(parsedArgs.getString("salt"), String.format("%s.%s", name, defStr)),
                            expConf, Collections.singleton(0));
                }
            }

            long totalTime = 0, minTime = Long.MAX_VALUE, maxTime = 0;
            int iterations = parsedArgs.getInt("iterations");
            final int worst05Cnt = Math.round(iterations * 0.05f);
            final PriorityQueue<Long> worst05Heap = new PriorityQueue<>(worst05Cnt);
            System.out.println("Starting " + iterations + " iterations...");
            for (int i=0; i < iterations; i++) {
                final Map<String, Object> inputMap = ImmutableMap.<String, Object>of(nsConf.get().unit, UUID.randomUUID().toString());
                long start = System.nanoTime();
                if (exp != null) {
                    new Interpreter(exp.def.getCopyOfScript(), exp.salt, inputMap, null).getParams();
                } else {
                    nsFact.getNamespace(name, inputMap).get();
                }
                long iterTime = System.nanoTime() - start;
                totalTime += iterTime;
                minTime = Math.min(minTime, iterTime);
                maxTime = Math.max(maxTime, iterTime);
                Long worstHead = worst05Heap.peek();
                if (worst05Heap.size() < worst05Cnt || worstHead < iterTime) {
                    if (worst05Heap.size() == worst05Cnt) {
                        worst05Heap.poll();
                    }
                    worst05Heap.add(iterTime);
                }
            }

            System.out.format("\nPerformed %d iterations in %d millis; min/max/avg/95pct: %d/%d/%d/%d micros\n",
                    iterations, totalTime / 1000000, minTime/1000, maxTime/1000,
                    (long)(0.001 * totalTime / iterations), worst05Heap.peek()/1000);
        } else {
            LOG.error("Namespace with name {} does not exist");
        }
    }

    private static Map<String, ?> evaluateStandalone(final Namespace parsedArgs, final String script,
                                                    final Map<String, Object> inputMap)
    {
        try {
            return new Interpreter(Helper.deepCopy(PlanoutDSLCompiler.dsl_to_json(script), null),
                    parsedArgs.getString("salt"), inputMap, null).getParams();
        } catch (ValidationException e) {
            LOG.error("Failed to compile script\n{}\n", e);
            return null;
        }
    }


    private PerfTool() {}

}
