package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpUnary;
import com.glassdoor.planout4j.util.Helper;

import java.util.Map;

/**
 * Numeric negative.
 */
public class Negative extends PlanOutOpUnary<Number> {

    public Negative(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Number unaryExecute(final Object value) {
        final Number n = getNumber(value, "value");
        return Helper.wrapNumber(0 - n.doubleValue(), !Helper.isRealNumber(n));
    }

    @Override
    protected String getUnaryString() {
        return "-";
    }
    
}
