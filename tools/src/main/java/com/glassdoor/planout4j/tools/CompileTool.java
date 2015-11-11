package com.glassdoor.planout4j.tools;

import java.io.*;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import com.google.common.io.CharStreams;

import com.glassdoor.planout4j.compiler.PlanoutDSLCompiler;
import com.glassdoor.planout4j.compiler.YAMLConfigParser;
import com.glassdoor.planout4j.config.ValidationException;

/**
 * Command-line interface to {@link com.glassdoor.planout4j.config.NamespaceConfigBuilder}.
 * Compiles either PlanOut DSL or PlanOut4J namespace config YAML with embedded DSL to JSON representation.
 */
public class CompileTool {
   
    private static final Logger LOG = LoggerFactory.getLogger(CompileTool.class);

    public static void configureArgsParser(final Subparsers subparsers) {
        final Subparser compile = subparsers.addParser("compile")
                .help("compiles PlanOut DSL (if input is file *not* ending in '.yaml', '.yml', or '.p4j' as well as when input is not a file) " +
                        "or PlanOut4J namespace config YAML with embedded PlanOut DSL (in all other cases) to JSON representation");
        compile.addArgument("input").help("either input file path or in-line DSL code");
        compile.addArgument("output").nargs("?").help("output file path (print to standard out if not specified)");
        compile.addArgument("--no-pretty").dest("pretty").action(Arguments.storeFalse()).help("do NOT pretty-print JSON");
    }

    public static void execute(final Namespace parsedArgs) throws IOException, ValidationException {
        final String input = parsedArgs.getString("input"), lcInput = input.toLowerCase();
        File file = new File(input);
        boolean yaml = false;
        if (lcInput.endsWith(".yaml") || lcInput.endsWith(".yml") || lcInput.endsWith(".p4j")) {
            yaml = true;
            LOG.debug("Assuming input is a namespace config file: {}", file.getAbsolutePath());
        } else if (file.canRead()) {
            LOG.debug("Assuming input is a DSL plain text file: {}", file.getAbsolutePath());
        } else {
            LOG.debug("Assuming input is a DSL code snippet: {}", input);
            file = null;
        }
        final String output = parsedArgs.getString("output");

        LOG.debug("Output goes to {}", output != null ? new File(output).getAbsolutePath() : "<STDOUT>");
        final Reader reader = file != null ? new FileReader(file) : new StringReader(input);
        final String namespace = yaml ? com.google.common.io.Files.getNameWithoutExtension(input) : null;
        try (final Writer writer = output != null ? new FileWriter(output) : new OutputStreamWriter(System.out)) {
            final Map<String, ?> config = yaml ?
                    new YAMLConfigParser().parseAndValidate(reader, namespace).getConfig() :
                    PlanoutDSLCompiler.dsl_to_json(CharStreams.toString(reader));
            writer.write(Planout4jTool.getConfigFormatter(parsedArgs).format(config));
        }
    }

    private CompileTool() {}

}
