package com.glassdoor.planout4j.demos;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Map;

import com.glassdoor.planout4j.Namespace;
import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.compiler.YAMLConfigParser;

/**
 * Planout4J with manually loading namespace (no config file, no backend classes utilized) demo.
 * Run using <code>mvn exec:java -Dexec.mainClass=com.glassdoor.planout4j.demos.ManualNoBackend [-Dexec.args=unit_value]</code>
 */
public class ManualNoBackend {

    private NamespaceConfig nsConf;

    ManualNoBackend(final NamespaceConfig nsConf) {
        this.nsConf = nsConf;
    }

    public void run(String unit) {
        Namespace ns = new Namespace(nsConf, Map.of("user_guid", unit), null);
        // get all params at once
        Map<String, ?> allParams = ns.getParams();
        // get params individually using defaults
        int pageSize = ns.getParam("page_size", 15);
        boolean showFullDetails = ns.getParam("full_details", false);
        System.out.println("allParams: " + allParams);
        System.out.println("pageSize: " + pageSize);
        System.out.println("showFullDetails: " + showFullDetails);
    }

    public static void main(String[] args) throws Exception {
        String nsName = "demo_namespace";
        new ManualNoBackend(new YAMLConfigParser().parseAndValidate(
                new FileReader(Paths.get("conf", nsName + ".yaml").toFile()), nsName))
                .run(args.length > 0 ? args[0] : "");
    }

}