package com.glassdoor.planout4j.config;

/**
 * Takes care of transporting PlanOut4J configuration (namespace definitions) from a source to a destination.
 * Performs validation and compilation from YAML with embedded PlanOut DSL into fully-resolved JSON.
 */
public interface Planout4jConfigShipper {

   /**
    * "Ships" configs from YAML at source to JSON at target.
    * @param dryRun if true, will <b>not</b> modify target backend (e.g. compile and validate only)
    * @throws ValidationException
    */
   void ship(final boolean dryRun) throws ValidationException;

}
