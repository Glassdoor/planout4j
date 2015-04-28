package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

/**
 * Tests if one operand is greater than or equal to the other.
 */
public class GreaterThanOrEqualTo extends PlanOutOpComparison {

    public GreaterThanOrEqualTo(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected boolean comparisonExecute(final int cmpResult) {
        return cmpResult >= 0;
    }

}
