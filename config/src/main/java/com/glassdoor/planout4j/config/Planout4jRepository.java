package com.glassdoor.planout4j.config;

import java.util.Map;

import com.glassdoor.planout4j.NamespaceConfig;

/**
 * Abstraction over Planout4j configuration repository.
 * Repository can retrieve configuration form a backend (such as file system, key-value store, etc.)
 * as well as "ship" configuration data from one backend to another.
 */
public interface Planout4jRepository {

    /**
     * Invoked by the API at the initial consuming app loading time and at regular refresh intervals.
     * @return Map of namespace name to NamespaceConfig object
     */
    Map<String, NamespaceConfig> loadAllNamespaceConfigs();

}