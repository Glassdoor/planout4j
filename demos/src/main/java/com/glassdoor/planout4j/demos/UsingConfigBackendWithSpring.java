package com.glassdoor.planout4j.demos;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.glassdoor.planout4j.Namespace;
import com.glassdoor.planout4j.NamespaceFactory;
import com.glassdoor.planout4j.config.ConfFileLoader;
import com.glassdoor.planout4j.spring.Planout4jAppContext;

/**
 * Planout4J with SpringFramework demo.
 * Run using <code>mvn exec:java -Dexec.mainClass=com.glassdoor.planout4j.demos.UsingConfigBackendWithSpring [-Dexec.args=unit_value]</code>
 */
public class UsingConfigBackendWithSpring {

    @Autowired
    private NamespaceFactory namespaceFactory;

    public void run(String unit) {
        Optional<Namespace> ns = namespaceFactory.getNamespace("demo_namespace", Map.of("user_guid", unit));
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
        new AnnotationConfigApplicationContext(AppCtx.class).getBean(UsingConfigBackendWithSpring.class)
                .run(args.length > 0 ? args[0] : "");
        System.exit(0);
    }

    /**
     * Dummy "application-specific" context; <code>@Import</code> is the important part.
     */
    @Configuration
    @Import(Planout4jAppContext.class)
    public static class AppCtx {
        @Bean
        public UsingConfigBackendWithSpring getUsingConfigBackendWithSpring() {
            return new UsingConfigBackendWithSpring();
        }
    }

}