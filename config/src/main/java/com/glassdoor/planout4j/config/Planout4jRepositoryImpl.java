package com.glassdoor.planout4j.config;

import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
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
      final Config config = ConfigFactory.load("planout4j-config");
      final String targetBackend = config.getString("target-backend");
      LOG.debug("Using {} as target backend", targetBackend);
      try {
         configBackend = ClassUtils.getClass(targetBackend).asSubclass(Planout4jConfigBackend.class).newInstance();
         configBackend.configure(config);
      } catch (Exception e) {
         throw new RuntimeException("Failed to load config backend " + targetBackend, e);
      }
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
