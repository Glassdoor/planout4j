package com.glassdoor.planout4j.tools;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * This is a wrapper around all command-line tools, providing <code>main()</code> method,
 * performing command-line parsing, and setting up logging.
 */
public class Planout4jTool {

    public static void main(final String[] args) {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("planout4j-tools")
                .description("Tools for working with planout4j namespaces");
        parser.addArgument("-c", "--config-file").dest("configFile").nargs(1).help("path to configuration file");
        try {
            Namespace res = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }

}
