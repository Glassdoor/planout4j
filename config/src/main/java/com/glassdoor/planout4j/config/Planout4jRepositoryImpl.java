package com.glassdoor.planout4j.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.compiler.JSONConfigParser;


public class Planout4jRepositoryImpl implements Planout4jRepository {
   
   private static final Logger LOG = LoggerFactory.getLogger(Planout4jRepositoryImpl.class);

   private Planout4jConfigBackend configBackend;

   public Planout4jRepositoryImpl() {
      final Config config = ConfFileLoader.loadConfig().getConfig("planout4j").getConfig("backend");
      configBackend = Planout4jConfigBackendFactory.createAndConfigure(config, config.getString("target"));
      LOG.info("Using {} as the backend", configBackend.persistenceDestination());
   }

   @Override
   public Map<String, NamespaceConfig> loadAllNamespaceConfigs() throws ValidationException {
      final Map<String, String> namespace2Config = configBackend.loadAll();
      LOG.info("Loaded {} planout4j namespace config(s) from {}", namespace2Config.size(), configBackend.persistenceDestination());
      if (LOG.isTraceEnabled()) {
         LOG.trace("Loaded planout4j namespace config(s) from {}:\n{}", configBackend.persistenceDestination(), namespace2Config);
      }
      return new JSONConfigParser().parseAndValidate(namespace2Config);
   }

}
