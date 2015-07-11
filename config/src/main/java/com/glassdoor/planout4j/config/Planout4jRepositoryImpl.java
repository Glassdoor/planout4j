package com.glassdoor.planout4j.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.compiler.Planout4jConfigParser;


public class Planout4jRepositoryImpl implements Planout4jRepository {

    private static final Logger LOG = LoggerFactory.getLogger(Planout4jRepositoryImpl.class);

    private Planout4jConfigBackend configBackend;
    private Planout4jConfigParser configParser;

    public Planout4jRepositoryImpl() {
        final Config config = ConfFileLoader.loadConfig().getConfig("planout4j").getConfig("backend");
        configBackend = Planout4jConfigBackendFactory.createAndConfigure(config, config.getString("runtimeRepo"));
        configParser = Planout4jConfigParser.createParser(config.getString("runtimeRepoParser"));
        LOG.info("Using {} as the backend and {} for parsing", configBackend.persistenceDestination(), configParser.getClass());
    }

    @Override
    public Map<String, NamespaceConfig> loadAllNamespaceConfigs() throws ValidationException {
        final Map<String, String> namespace2Config = configBackend.loadAll();
        LOG.info("Loaded {} planout4j namespace config(s) from {}", namespace2Config.size(), configBackend.persistenceDestination());
        if (LOG.isTraceEnabled()) {
            LOG.trace("Loaded planout4j namespace config(s) from {}:\n{}", configBackend.persistenceDestination(), namespace2Config);
        }
        return configParser.parseAndValidate(namespace2Config);
    }

}
