package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpUnary;
import com.glassdoor.planout4j.util.Helper;

import java.util.Map;

/**
 * Logical NOT.
 */
public class Not extends PlanOutOpUnary<Boolean> {

    public Not(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Boolean unaryExecute(final Object value) {
        return !Helper.asBoolean(value);
    }

    @Override
    protected String getUnaryString() {
        return "!";
    }

}
