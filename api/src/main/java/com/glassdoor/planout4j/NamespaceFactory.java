package com.glassdoor.planout4j;

import java.util.Map;

import com.google.common.base.Optional;

/**
 * Primary entry point into Planout4j library.
 * Responsible for maintaining in-memory mapping of all namespace names to the respective {@link NamespaceConfig} objects.
 */
public interface NamespaceFactory {

    /**
     * Constructs a namespace. Verifies the input is sane (has entry for the primary unit).
     * @param name Valid namespace name
     * @param paramName2valueMap Map of string parameter names to parameter values
     * @param overrides override parameter names/values to enable "freezing"
     *                  certain computed parameters, e.g. via query string; optional
     * @throws IllegalArgumentException in case of bad input (e.g. invalid namespace name, empty input, etc.)
     */
    Optional<Namespace> getNamespace(String name, Map<String, ?> paramName2valueMap, Map<String, ?> overrides);

    /**
     * Constructs a namespace. Verifies the input is sane (has entry for the primary unit).
     * @param name Valid namespace name
     * @param paramName2valueMap Map of string parameter names to parameter values
     * @throws IllegalArgumentException in case of bad input (e.g. invalid namespace name, empty input, etc.)
     */
    Optional<Namespace> getNamespace(String name, Map<String, ?> paramName2valueMap);

    /**
     * @return number of available namespaces
     */
    int getNamespaceCount();
    
}
