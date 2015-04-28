package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpBinary;
import com.glassdoor.planout4j.util.Helper;

import java.util.Map;

/**
 * Modulo operator (using long integers).
 */
public class Mod extends PlanOutOpBinary<Number> {

    public Mod(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Number binaryExecute(final Object left, final Object right) {
        return Helper.wrapNumber(getNumber(left, "left").longValue() % getNumber(right, "right").longValue(), true);
    }

}
