package com.glassdoor.planout4j.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.glassdoor.planout4j.tools.Planout4jCompilerTool;


public class Planout4jConfigShipperImpl implements Planout4jConfigShipper {

   private final Logger LOG = LoggerFactory.getLogger(getClass());

   private Planout4jConfigBackend sourceBackend;
   private Planout4jConfigBackend targetBackend;

   public Planout4jConfigShipperImpl() {
      final Config config = ConfigFactory.load("planout4j-shipper").getConfig("planout4j").getConfig("backend");
      sourceBackend = Planout4jConfigBackendFactory.createAndConfigure(config, config.getString("source"));
      targetBackend = Planout4jConfigBackendFactory.createAndConfigure(config, config.getString("target"));
      LOG.info("Configured to ship from {} to {}", sourceBackend.persistenceDestination(), targetBackend.persistenceDestination());
   }

   @Override
   public void ship() throws ValidationException {
      final Map<String, String> namespace2Config = sourceBackend.loadAll();
      LOG.info("Completed reading config from {}\n{}", sourceBackend.persistenceDestination(), namespace2Config.keySet());

      final Map<String, String> compiledNamespace2Config = new HashMap<>();
      Planout4jCompilerTool.compilePlanout4jConfig(namespace2Config, compiledNamespace2Config);

      LOG.info("Persisting compiled config to {}", targetBackend.persistenceDestination());
      LOG.debug("Compiled config:\n{}", compiledNamespace2Config);
      targetBackend.persist(compiledNamespace2Config);
   }

}
