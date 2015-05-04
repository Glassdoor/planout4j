package com.glassdoor.planout4j.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.compiler.Planout4jConfigParser;


public class Planout4jRepositoryImpl implements Planout4jRepository {
   
   private static final Logger LOG = LoggerFactory.getLogger(Planout4jRepositoryImpl.class);

   private Planout4jConfigBackend configBackend;

   public Planout4jRepositoryImpl() {
      final Config config = ConfigFactory.load("planout4j-repository").getConfig("planout4j").getConfig("backend");
      configBackend = Planout4jConfigBackendFactory.createAndConfigure(config, config.getString("source"));
      LOG.info("Using {} as the backend", configBackend.persistenceDestination());
   }

   @Override
   public Map<String, NamespaceConfig> loadAllNamespaceConfigs() {
      Map<String, String> namespace2Config = configBackend.loadAll();
      if (LOG.isTraceEnabled()) {
         LOG.trace("Loaded planout4j config from {}: {}", configBackend.persistenceLayer(), namespace2Config);
      }
      try {
         return Planout4jConfigParser.parseAndValidateJSON(namespace2Config);
      } catch (ValidationException e) {
         throw new RuntimeException("Unable to load all namespace configs", e);
      }
   }

}
