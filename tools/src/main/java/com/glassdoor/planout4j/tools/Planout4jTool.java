package com.glassdoor.planout4j.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

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

import com.glassdoor.planout4j.config.ConfFileLoader;
import com.glassdoor.planout4j.util.VersionLogger;

import static java.lang.String.format;

/**
 * This is a wrapper around all command-line tools, providing <code>main()</code> method,
 * performing command-line parsing, and setting up logging.
 */
public class Planout4jTool {

    public static void main(final String[] args) {
        final Properties versions = VersionLogger.properties("tools");
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("planout4j-tools")
                .version(format("${prog} %s built on %s", versions.getProperty("git.commit.id.describe"),
                        versions.getProperty("git.build.time")))
                .description("Tools for working with planout4j namespaces");
        parser.addArgument("-v", "--version").action(Arguments.version());
        parser.addArgument("-c", "--config-file").nargs("?").help("path to configuration file");
        parser.addArgument("-l", "--log-level").nargs("?").setDefault("DEBUG").help("planout4j log level (default: DEBUG)");
        final Subparsers subparsers = parser.addSubparsers().title("tools").metavar("tool").dest("tool")
                .description("available tools (invoke with '--help' to see specific help)");

        CompileTool.configureArgsParser(subparsers);
        ShipTool.configureArgsParser(subparsers);
        NslistTool.configureArgsParser(subparsers);
        EvalTool.configureArgsParser(subparsers);
        PerfTool.configureArgsParser(subparsers);

        String tool = null;
        try {
            final Namespace parsedArgs = parser.parseArgs(args);
            tool = parsedArgs.getString("tool");
            setupLoggin(parsedArgs.getString("log_level"));
            setSystemProperties(parsedArgs);
            LoggerFactory.getLogger(Planout4jTool.class).trace(parsedArgs.toString());
            LoggerFactory.getLogger(Planout4jTool.class).info("Starting {} tool", tool);
            ClassUtils.getClass(format("%s.%sTool",
                    Planout4jTool.class.getPackage().getName(), StringUtils.capitalize(tool)))
                    .getMethod("execute", Namespace.class).invoke(null, parsedArgs);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            System.err.println("UNEXPECTED: Failed to load tool " + tool);
            e.printStackTrace(System.err);
            System.exit(2);
        } catch (InvocationTargetException e) {
            System.err.println("Failed to execute tool " + tool);
            e.getCause().printStackTrace(System.err);
            System.exit(3);
        }
    }


    private static final Map<String, String> ARG2SYSPROP = new ImmutableMap.Builder<String, String>()
            .put("config_file",    ConfFileLoader.P4J_CONF_FILE)
            .put("target_backend", "planout4j.backend.target")
            .put("runtime_repo",   "planout4j.backend.runtimeRepo")
            .put("parser_type",    "planout4j.backend.runtimeRepoParser")
            .put("source_dir",     "planout4j.backend.sourceConfDir")
            .put("dest_dir",       "planout4j.backend.compiledConfDir")
            .put("redis_host",     "planout4j.backend.redis.host")
            .put("redis_key",      "planout4j.backend.redis.key")
            .build();

    static void setSystemProperties(final Namespace parsedArgs) {
        for (String argName : ARG2SYSPROP.keySet()) {
            final String value = parsedArgs.getString(argName);
            if (value != null) {
                System.setProperty(ARG2SYSPROP.get(argName), value);
            }
        }
    }

    static void addBackendArgs(final ArgumentParser parser, final boolean includeSourceAndTarget) {
        if (includeSourceAndTarget) {
            parser.addArgument("-s", "--source-dir").help("source directory for the file backend");
            parser.addArgument("--target-backend").help("specify target backend, e.g. redis or compiledFiles (it must be defined in config file)");
        } else {
            parser.addArgument("--runtime-repo").help("specify runtime repository, e.g. sourceFiles, redis or compiledFiles (it must be defined in config file)");
            parser.addArgument("--parser-type").choices("json", "yaml").help("specify whether the runtime repo contains JSON or YAML");
        }
        parser.addArgument("-d", "--dest-dir").help("destination directory for the file backend");
        parser.addArgument("--redis-host").help("redis host to use with redis backend");
        parser.addArgument("--redis-key").help("redis key to use with redis backend");
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
