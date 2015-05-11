package com.glassdoor.planout4j.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.LoggerFactory;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is a wrapper around all command-line tools, providing <code>main()</code> method,
 * performing command-line parsing, and setting up logging.
 */
public class Planout4jTool {

    public static void main(final String[] args) {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("planout4j-tools")
                .description("Tools for working with planout4j namespaces");
        parser.addArgument("-c", "--config-file").nargs("?").help("path to configuration file");
        parser.addArgument("-l", "--log-level").nargs("?").setDefault("DEBUG").help("planout4j log level (default: DEBUG)");
        final Subparsers subparsers = parser.addSubparsers().title("tools").metavar("tool").dest("tool")
                .description("available tools (invoke with '--help' to see specific help)");

        final Subparser compile = subparsers.addParser("compile")
                .help("compiles PlanOut DSL (if input is file *not* ending in '.yaml', '.yml', or '.p4j' as well as when input is not a file) " +
                        "or PlanOut4J namespace config YAML with embedded PlanOut DSL (in all other cases) to JSON representation");
        compile.addArgument("input").help("either input file path or in-line DSL code");
        compile.addArgument("output").nargs("?").help("output file path (print to standard out if not specified)");
        compile.addArgument("--no-pretty").dest("pretty").action(Arguments.storeFalse()).help("do NOT pretty-print JSON");

        final Subparser ship = subparsers.addParser("ship")
                .help("compiles all namespace config YAML files in the source backend to JSON and stores results in the target backend");
        ship.addArgument("--dry-run").action(Arguments.storeTrue()).help("do not modify target backend (validate source configs only)");
        ship.addArgument("--target-backend").help("specify target backend, e.g. redis or compiled_files (it must be defined in config file)");
        ship.addArgument("-s", "--source-dir").help("source directory for the file backend");
        ship.addArgument("-d", "--dest-dir").help("destination directory for the file backend");

        final Subparser nslist = subparsers.addParser("nslist")
                .help("lists all namespaces (name + short summary) in the target (effective) backend");
        nslist.addArgument("--target-backend").help("specify target backend, e.g. redis or compiled_files (it must be defined in config file)");
        nslist.addArgument("-d", "--dest-dir").help("destination directory for the file backend");
        nslist.addArgument("-n", "--name").help("regular expression or substring to match namespace names against");
        nslist.addArgument("--full").action(Arguments.storeTrue()).help("display full namespace config");
        nslist.addArgument("--no-pretty").dest("pretty").action(Arguments.storeFalse())
                .help("do NOT pretty-print JSON when printing full config");

        final Subparser eval = subparsers.addParser("eval")
                .help("evaluates namespace, experiment, or code snippet");
        eval.addArgument("--target-backend").help("specify target backend, e.g. redis or compiled_files (it must be defined in config file)");
        eval.addArgument("-d", "--dest-dir").help("destination directory for the file backend");
        final MutuallyExclusiveGroup nameOrScript = eval.addMutuallyExclusiveGroup("evaluation object");
        nameOrScript.addArgument("-n", "--name").help("namespace name");
        final MutuallyExclusiveGroup experimentOrDefinition = eval.addMutuallyExclusiveGroup("additional selectors within namespace");
        experimentOrDefinition.addArgument("--exp").help("experiment name (requires --name NAME)");
        experimentOrDefinition.addArgument("--def").help("experiment definition key (requires --name NAME)");
        nameOrScript.addArgument("--script").help("planout DSL script");
        eval.addArgument("input").nargs("+").help("input parameters in the form of KEY=VALUE");

        String tool = null;
        try {
            final Namespace parsedArgs = parser.parseArgs(args);
            tool = parsedArgs.getString("tool");
            setupLoggin(parsedArgs.getString("log_level"));
            setSystemProperties(parsedArgs);
            LoggerFactory.getLogger(Planout4jTool.class).trace(parsedArgs.toString());
            LoggerFactory.getLogger(Planout4jTool.class).info("Starting {} tool", tool);
            ClassUtils.getClass(String.format("%s.%sTool",
                    Planout4jTool.class.getPackage().getName(), StringUtils.capitalize(tool)))
                    .getMethod("execute", Namespace.class).invoke(null, parsedArgs);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            System.err.println("UNEXPECTED: Failed to load tool " + tool);
            e.printStackTrace(System.err);
        } catch (InvocationTargetException e) {
            System.err.println("Failed to execute tool " + tool);
            e.getCause().printStackTrace(System.err);
        }
    }


    private static final Map<String, String> ARG2SYSPROP = ImmutableMap.of(
            "config_file", "planout4jConfigFile",
            "target_backend", "planout4j.backend.target",
            "source_dir", "planout4j.backend.sourceConfDir",
            "dest_dir", "planout4j.backend.compiledConfDir");

    static void setSystemProperties(final Namespace parsedArgs) {
        for (String argName : ARG2SYSPROP.keySet()) {
            final String value = parsedArgs.getString(argName);
            if (value != null) {
                System.setProperty(ARG2SYSPROP.get(argName), value);
            }
        }
    }

    static Gson getGson(final Namespace parsedArgs) {
        final GsonBuilder builder = new GsonBuilder();
        if (parsedArgs.getBoolean("pretty")) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }


    private static void setupLoggin(final String level) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(
                new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN), ConsoleAppender.SYSTEM_ERR));
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("com.glassdoor.planout4j").setLevel(Level.toLevel(level));
    }

}
