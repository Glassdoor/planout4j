package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpUnary;
import com.glassdoor.planout4j.util.Helper;

import java.util.Map;

/**
 * Rounding (double to long).
 */
public class Round extends PlanOutOpUnary<Number> {

    public Round(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Number unaryExecute(final Object value) {
        return Helper.wrapNumber(Math.round(getNumber(value, "value").doubleValue()), true);
    }

}
