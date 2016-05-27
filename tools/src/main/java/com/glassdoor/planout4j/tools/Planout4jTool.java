package com.glassdoor.planout4j.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

import com.glassdoor.planout4j.config.ConfFileLoader;
import com.glassdoor.planout4j.config.ConfigFormatter;
import com.glassdoor.planout4j.config.JsonConfigFormatterImpl;
import com.glassdoor.planout4j.util.VersionLogger;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * This is a wrapper around all command-line tools, providing <code>main()</code> method,
 * performing command-line parsing, and setting up logging.
 */
public final class Planout4jTool {

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
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
        } catch (final ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            System.err.println("UNEXPECTED: Failed to load tool " + tool);
            e.printStackTrace(System.err);
            System.exit(2);
        } catch (final InvocationTargetException e) {
            System.err.println("Failed to execute tool " + tool);
            e.getCause().printStackTrace(System.err);
            System.exit(3);
        }
    }


    private static final Map<String, String> ARG2SYSPROP = new Builder<String, String>()
            .put("config_file",    ConfFileLoader.P4J_CONF_FILE)
            .put("target_backend", "planout4j.backend.target")
            .put("runtime_repo",   "planout4j.backend.runtimeRepo")
            .put("parser_type",    "planout4j.backend.runtimeRepoParser")
            .put("source_dir",     "planout4j.backend.sourceConfDir")
            .put("dest_dir",       "planout4j.backend.compiledConfDir")
            .put("redis_host",     "planout4j.backend.redis.host")
            .put("redis_port",     "planout4j.backend.redis.port")
            .put("redis_key",      "planout4j.backend.redis.key")
            .build();

    static void setSystemProperties(final Namespace parsedArgs) {
        for (final String argName : ARG2SYSPROP.keySet()) {
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
        parser.addArgument("--redis-port").help("redis port number to use with redis backend");
        parser.addArgument("--redis-key").help("redis key to use with redis backend");
    }

    static Gson getGson(final Namespace parsedArgs) {
        final GsonBuilder builder = new GsonBuilder();
        if (parsedArgs.getBoolean("pretty")) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }

    static ConfigFormatter getConfigFormatter(final Namespace parsedArgs) {
        final boolean pretty = parsedArgs.getBoolean("pretty") == Boolean.TRUE;
        return new JsonConfigFormatterImpl(pretty);
    }

    private static void setupLoggin(final String level) {
        Logger.getRootLogger().addAppender(new ConsoleAppender(
                new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN), ConsoleAppender.SYSTEM_ERR));
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("com.glassdoor.planout4j").setLevel(Level.toLevel(level));
    }


    private Planout4jTool() {}

}
