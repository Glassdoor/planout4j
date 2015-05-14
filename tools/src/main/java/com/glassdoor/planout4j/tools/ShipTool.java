package com.glassdoor.planout4j.tools;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import com.glassdoor.planout4j.config.Planout4jConfigShipperImpl;
import com.glassdoor.planout4j.config.ValidationException;

/**
 * Command-line interface to {@link com.glassdoor.planout4j.config.Planout4jConfigShipperImpl}.
 * Can be invoked manually or from a CI to read namespace configs from source,
 * compile & validate them, and write into target backend.
 */
public class ShipTool {

    private static final Logger LOG = LoggerFactory.getLogger(ShipTool.class);

    public static void configureArgsParser(final Subparsers subparsers) {
        final Subparser ship = subparsers.addParser("ship")
                .help("compiles all namespace config YAML files in the source backend to JSON and stores results in the target backend");
        ship.addArgument("--dry-run").action(Arguments.storeTrue()).help("do not modify target backend (validate source configs only)");
        ship.addArgument("--target-backend").help("specify target backend, e.g. redis or compiled_files (it must be defined in config file)");
        ship.addArgument("-s", "--source-dir").help("source directory for the file backend");
        ship.addArgument("-d", "--dest-dir").help("destination directory for the file backend");
    }

    public static void execute(final Namespace parsedArgs) throws IOException, ValidationException {
        new Planout4jConfigShipperImpl().ship(parsedArgs.getBoolean("dry_run"));
    }

    private ShipTool() {}

}
