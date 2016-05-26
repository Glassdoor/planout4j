package com.glassdoor.planout4j.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.compiler.Planout4jConfigParser;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;


public class Planout4jRepositoryImpl implements Planout4jRepository {

    private static final Logger LOG = LoggerFactory.getLogger(Planout4jRepositoryImpl.class);

    private Planout4jConfigBackend configBackend;
    private Planout4jConfigParser configParser;

    public Planout4jRepositoryImpl() {
        final Config config = ConfFileLoader.loadConfig().getConfig("planout4j").getConfig("backend");
        setBackendAndParser(
                Planout4jConfigBackendFactory.createAndConfigure(config, config.getString("runtimeRepo")),
                Planout4jConfigParser.createParser(config.getString("runtimeRepoParser")));
    }

    public Planout4jRepositoryImpl(final Planout4jConfigBackend configBackend, final Planout4jConfigParser configParser) {
        setBackendAndParser(configBackend, configParser);
    }

    private void setBackendAndParser(final Planout4jConfigBackend configBackend, final Planout4jConfigParser configParser) {
        this.configBackend = requireNonNull(configBackend);
        this.configParser = requireNonNull(configParser);
        LOG.info("Using {} as the backend and {} for parsing", configBackend.persistenceDestination(), configParser.getClass());
    }

    @Override
    public Map<String, NamespaceConfig> loadAllNamespaceConfigs() throws ValidationException {
        final Map<String, String> namespace2Config = configBackend.loadAll();
        final List<String> sortedNamespaceKeys = new ArrayList<>(namespace2Config.keySet());
        Collections.sort(sortedNamespaceKeys);
        LOG.info("Loaded {} planout4j namespace config(s) from {} ; namespaces: {}",
                namespace2Config.size(), configBackend.persistenceDestination(), sortedNamespaceKeys);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Loaded planout4j namespace config(s) from {}:\n{}", configBackend.persistenceDestination(), namespace2Config);
        }
        return configParser.parseAndValidate(namespace2Config);
    }

}
