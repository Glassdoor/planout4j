package com.glassdoor.planout4j.config;

import java.util.Map;

import com.typesafe.config.Config;

/**
 * Abstract interface to read / write Planout4j config from / to a persistence layer without any data modification.
 * 
 */
public interface Planout4jConfigBackend {

   /**
    * Set backend's properties
    * @param config
    */
   void configure(Config config);

   /**
    * @return a map of namespace name to content in the corresponding persistence layer
    */
   Map<String, String> loadAll();

   /**
    * @param namespace2Content namespace2Content to be stored
    */
   void persist(Map<String, String> namespace2Content);

   /**
    * Convenience debug info
    * @return persistence layer of data
    */
   String persistenceLayer();

   /**
    * @return convenience debug info: human readable form of where data gets stored
    */
   String persistenceDestination();
}
