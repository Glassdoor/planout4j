package com.glassdoor.planout4j.config;

import org.apache.commons.lang3.ClassUtils;
import com.typesafe.config.Config;

/**
 * Helper class responsible for instantiating and configuring a backend.
 */
public class Planout4jConfigBackendFactory {

    public static Planout4jConfigBackend createAndConfigure(final Config config, final String backend) {
        String backendClass = "undefined";
        try {
            final Config backendConfig = config.getConfig(backend);
            backendClass = backendConfig.getString("class");
            final Planout4jConfigBackend configBackend = ClassUtils.getClass(backendClass)
                    .asSubclass(Planout4jConfigBackend.class).newInstance();
            configBackend.configure(backendConfig);
            return configBackend;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to load or configure backend %s (class: %s)", backend, backendClass), e);
        }
    }

}
