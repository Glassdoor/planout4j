package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpBinary;

import java.util.Map;

/**
 * Real division operator.
 */
public class Divide extends PlanOutOpBinary<Double> {

    public Divide(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Double binaryExecute(final Object left, final Object right) {
        return getNumber(left, "left").doubleValue() / getNumber(right, "right").doubleValue();
    }

}
