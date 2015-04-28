package com.glassdoor.planout4j.planout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Common functionality shared by {@link com.glassdoor.planout4j.planout.Interpreter}
 * and {@link com.glassdoor.planout4j.planout.Assignment} classes.
 */
public abstract class Mapper {

    protected Object experimentSalt;

    protected Mapper(final Object experimentSalt) {
        this.experimentSalt = experimentSalt;
    }

    public abstract Object evaluate(Object expression);

    public Object getExperimentSalt() {
        return experimentSalt;
    }

    public void setExperimentSalt(final Object experimentSalt) {
        this.experimentSalt = experimentSalt;
    }

    /**
     * Check to see if a variable has an override.
     * @param name the variable name
     * @return true, if the variable has override; false, otherwise
     */
    public abstract boolean hasOverride(String name);

    /**
     * Check if a variable exists in the PlanOut environment.
     * @param name the variable name
     * @return true if exists, false otherwise
     */
    public abstract boolean has(String name);

    /**
     * Get a variable from the PlanOut environment falling back on inputs and then on the optional default.
     * @param name the variable name
     * @param def optional default fallback
     * @return variable value
     */
    public abstract Object get(String name, Object def);

    /**
     * Get a variable from the PlanOut environment falling back on inputs.
     * @param name the variable name
     * @return variable value
     */
    public Object get(String name) {
        return get(name, null);
    }

    /**
     * Set a variable in the PlanOut environment.
     * @param name the variable name
     * @param value value to set to
     * @return this instance
     */
    public abstract Mapper set(String name, Object value);

    /**
     * Null-safe map copying.
     * @param original map to copy, may be null
     * @return if original is null, new empty map; otherwise, new map with all original entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> copyOf(final Map<K, V> original) {
        return new HashMap<>(original == null ? Collections.EMPTY_MAP : original);
    }

}
