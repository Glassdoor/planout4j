package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpBinary;

import java.util.Map;
import java.util.Objects;

/**
 * Tests two objects for equality.
 */
public class Equals extends PlanOutOpBinary<Boolean> {

    public Equals(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Boolean binaryExecute(final Object left, final Object right) {
        if (left instanceof Number && right instanceof Number) {
            return ((Number)left).doubleValue() == ((Number)right).doubleValue();
        }
        return Objects.equals(left, right);
    }

    @Override
    protected String getInfixString() {
        return "==";
    }

}
