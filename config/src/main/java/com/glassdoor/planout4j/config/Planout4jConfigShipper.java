package com.glassdoor.planout4j.config;

/**
 * Takes care of transporting PlanOut4J configuration (namespace definitions) from a source to a destination.
 * Performs validation and compilation from YAML with embedded PlanOut DSL into fully-resolved JSON.
 */
public interface Planout4jConfigShipper {

   void ship() throws ValidationException;

}
