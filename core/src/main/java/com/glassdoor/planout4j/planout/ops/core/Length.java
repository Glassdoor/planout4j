package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpUnary;
import com.glassdoor.planout4j.util.Helper;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Length of a string, list, or map.
 */
public class Length extends PlanOutOpUnary<Integer> {

    public Length(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Integer unaryExecute(final Object value) {
        if (value == null) {
            return 0;
        } else if (value instanceof List) {
            return ((List) value).size();
        } else if (value instanceof Map) {
            return ((Map)value).size();
        } else if (value instanceof String) {
            return ((String)value).length();
        }
        throw new IllegalStateException(format("%s: don't know how to obtain length of an instance of %s",
                getClass(), Helper.getClassName(value)));
    }

}
