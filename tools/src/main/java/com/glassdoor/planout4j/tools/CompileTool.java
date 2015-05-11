package com.glassdoor.planout4j.tools;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.argparse4j.inf.Namespace;
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
            writer.write(Planout4jTool.getGson(parsedArgs).toJson(yaml ?
                    new YAMLConfigParser().parseAndValidate(reader, namespace) :
                    PlanoutDSLCompiler.dsl_to_json(CharStreams.toString(reader))));
        }
    }

    private CompileTool() {}

}
