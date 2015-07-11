package com.glassdoor.planout4j.demos;

import java.nio.file.Paths;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import com.glassdoor.planout4j.Namespace;
import com.glassdoor.planout4j.NamespaceFactory;
import com.glassdoor.planout4j.SimpleNamespaceFactory;
import com.glassdoor.planout4j.config.ConfFileLoader;

/**
 * Planout4J without SpringFramework but with configured backend demo.
 * Run using <code>mvn exec:java -Dexec.mainClass=com.glassdoor.planout4j.demos.UsingConfigBackendNoSpring [-Dexec.args=unit_value]</code>
 */
public class UsingConfigBackendNoSpring {

    private NamespaceFactory namespaceFactory;

    UsingConfigBackendNoSpring(NamespaceFactory namespaceFactory) {
        this.namespaceFactory = namespaceFactory;
    }

    public void run(String unit) {
        Optional<Namespace> ns = namespaceFactory.getNamespace("demo_namespace", ImmutableMap.of("user_guid", unit));
        if (ns.isPresent()) {
            // get all params at once
            Map<String, ?> allParams = ns.get().getParams();
            // get params individually using defaults
            int pageSize = ns.get().getParam("page_size", 15);
            boolean showFullDetails = ns.get().getParam("full_details", false);
            System.out.println("allParams: " + allParams);
            System.out.println("pageSize: " + pageSize);
            System.out.println("showFullDetails: " + showFullDetails);
        }
    }

    public static void main(String[] args) {
        System.setProperty(ConfFileLoader.P4J_CONF_FILE, Paths.get("conf", "demo_planout4j.conf").toString());
        new UsingConfigBackendNoSpring(new SimpleNamespaceFactory()).run(args.length > 0 ? args[0] : "");
        System.exit(0);
    }

}