package com.glassdoor.planout4j.config;

import java.util.Map;

import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.compiler.YAMLConfigParser;


public class Planout4jConfigShipperImpl implements Planout4jConfigShipper {

   private final Logger LOG = LoggerFactory.getLogger(getClass());

   private Planout4jConfigBackend sourceBackend;
   private Planout4jConfigBackend targetBackend;

   public Planout4jConfigShipperImpl() {
      final Config config = ConfFileLoader.loadConfig().getConfig("planout4j").getConfig("backend");
      sourceBackend = Planout4jConfigBackendFactory.createAndConfigure(config, config.getString("source"));
      targetBackend = Planout4jConfigBackendFactory.createAndConfigure(config, config.getString("target"));
      LOG.info("Configured to ship from {} to {}", sourceBackend.persistenceDestination(), targetBackend.persistenceDestination());
   }

   @Override
   public void ship(final boolean dryRun) throws ValidationException {
      final Map<String, String> namespace2Config = sourceBackend.loadAll();
      LOG.info("Completed reading config from {} - {} namespace(s):\n{}",
              sourceBackend.persistenceDestination(), namespace2Config.size(), namespace2Config.keySet());

      final Map<String, NamespaceConfig> compiledNamespace2Config = new YAMLConfigParser().parseAndValidate(namespace2Config);

      if (dryRun) {
         LOG.info("Dry-run is set, NOT persisting");
      } else {
         LOG.info("Persisting compiled config to {}", targetBackend.persistenceDestination());
         LOG.debug("Compiled config:\n{}", compiledNamespace2Config);
         targetBackend.persist(Maps.transformValues(compiledNamespace2Config, new Function<NamespaceConfig, String>() {
            @Override
            public String apply(final NamespaceConfig input) {
               return JSONValue.toJSONString(input.getConfig());
            }
         }));
      }
   }

}
