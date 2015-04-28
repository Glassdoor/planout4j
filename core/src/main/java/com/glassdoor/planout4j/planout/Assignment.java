package com.glassdoor.planout4j.planout;

import com.glassdoor.planout4j.planout.ops.random.PlanOutOpRandom;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;

/**
 * The Assignment class is the main work horse that lets you to execute random operators
 * using the names of variables being assigned as salts.
 */
public class Assignment extends Mapper {

    private static final Map<String, String> RESERVED_NAMES = ImmutableMap.of(
            "data", "data", "_overrides", "overrides", "experiment_salt", "experimentSalt");

    private final Map<String, Object> data;
    private final Map<String, Object> overrides;

    public Assignment(final Object experimentSalt, final Map<String, Object> overrides) {
        super(experimentSalt);
        this.overrides = copyOf(overrides);
        this.data = copyOf(overrides);
    }

    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }

    @Override
    public boolean hasOverride(final String name) {
        return overrides.containsKey(name);
    }

    @Override
    public Object get(final String name, final Object def) {
        Object value;
        if (RESERVED_NAMES.containsKey(name)) {
            try {
                value = FieldUtils.readField(this, RESERVED_NAMES.get(name), true);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(format("Failed to get Assignment.%s", name), e);
            }
        }
        else {
            value = data.get(name);
        }
        return value == null ? def : value;
    }

    @Override
    public boolean has(final String name) {
        return data.containsKey(name);
    }

    @Override
    public Mapper set(final String name, final Object value) {
        if (RESERVED_NAMES.containsKey(name)) {
            try {
                FieldUtils.writeField(this, RESERVED_NAMES.get(name), value, true);
            } catch (Exception e) {
                throw new RuntimeException(format("Failed to set Assignment.%s to %s", name, value), e);
            }
        }
        if (overrides.containsKey(name)) {
            return this;
        }
        if (value instanceof PlanOutOpRandom) {
            final PlanOutOpRandom random = (PlanOutOpRandom)value;
            if (!random.hasArg("salt")) {
                random.setArg("salt", name);
            }
            data.put(name, random.execute(this));
        } else {
            data.put(name, value);
        }
        return this;
    }

    @Override
    public Object evaluate(final Object expression) {
        return expression;
    }

}
