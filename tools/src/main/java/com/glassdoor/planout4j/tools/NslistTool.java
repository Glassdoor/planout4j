package com.glassdoor.planout4j.tools;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.config.ConfigFormatter;
import com.glassdoor.planout4j.config.Planout4jRepositoryImpl;
import com.glassdoor.planout4j.config.ValidationException;

/**
 * Command-line interface for getting information about namespaces in the target backend.
 * Supports listing all namespaces (with short summary), filtering by name pattern,
 * and displaying full details.
 */
public class NslistTool {

    private static final Logger LOG = LoggerFactory.getLogger(NslistTool.class);

    private static enum DisplayMode { summary, full, experiments }

    public static void configureArgsParser(final Subparsers subparsers) {
        final Subparser nslist = subparsers.addParser("nslist")
                .help("lists all namespaces in the target (effective) backend with specified level of details");
        Planout4jTool.addBackendArgs(nslist, false);
        nslist.addArgument("-n", "--name").help("regular expression or substring to match namespace names against");
        nslist.addArgument("-m", "--mode").type(DisplayMode.class).choices(DisplayMode.values()).setDefault(DisplayMode.summary)
                .help("display mode (defaults to summary)");
        nslist.addArgument("--no-pretty").dest("pretty").action(Arguments.storeFalse())
                .help("do NOT pretty-print JSON when printing full config");
    }


    public static void execute(final Namespace parsedArgs) throws IOException, ValidationException {
        final Map<String, NamespaceConfig> namespaces = new Planout4jRepositoryImpl().loadAllNamespaceConfigs();
        final DisplayMode mode = parsedArgs.get("mode");
        final Table table = new Table(5, BorderStyle.CLASSIC, ShownBorders.ALL);
        addCells(table, "name", "total segs", "used segs", "definitions", "active experiments");
        final ConfigFormatter configFormatter = Planout4jTool.getConfigFormatter(parsedArgs);

        final String namePatternStr = StringUtils.lowerCase(parsedArgs.getString("name"));
        Pattern namePattern = null;
        try {
            if (namePatternStr != null && !StringUtils.isAlphanumeric(namePatternStr)) {
                LOG.debug("name pattern '{}' is not alphanumeric, assuming a regex", namePatternStr);
                namePattern = Pattern.compile(namePatternStr, Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException e) {
            LOG.warn("Invalid name regex, listing all namespace", e);
        }

        for (String name : new TreeSet<>(namespaces.keySet())) {
            NamespaceConfig nsConf = namespaces.get(name);
            if (namePatternStr == null
                    || namePattern != null && namePattern.matcher(name).matches()
                    || name.toLowerCase().contains(namePatternStr))
            {
                switch (mode) {
                    case full:
                        System.out.printf("********************** START of %s *********************\n", name);
                        System.out.println(configFormatter.format(nsConf.getConfig()));
                        System.out.printf("*********************** END of %s **********************\n", name);
                        break;
                    case summary:
                        addCells(table, name, nsConf.getTotalSegments(), nsConf.getUsedSegments(),
                                nsConf.getExperimentDefsCount(), nsConf.getActiveExperimentsCount());
                        break;
                    case experiments:
                        for (String experimentName : nsConf.getActiveExperimentNames()) {
                            System.out.println(name + "," + experimentName);
                        }
                        break;
                }
            } else {
                LOG.trace("namespace name {} doesn't match pattern {}", name, namePatternStr);
            }
        }
        if (mode == DisplayMode.summary) {
            System.out.println(table.render());
        }
    }

    private static void addCells(final Table table, final Object... cells) {
        for (Object cell : cells) {
            table.addCell(cell.toString(), new CellStyle(
                    cell instanceof Number ? CellStyle.HorizontalAlign.right : CellStyle.HorizontalAlign.left));
        }
    }

    private NslistTool() {}

}
