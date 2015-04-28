package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

/**
 * Tests if one operand is less than the other.
 */
public class LessThan extends PlanOutOpComparison {

    public LessThan(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected boolean comparisonExecute(final int cmpResult) {
        return cmpResult < 0;
    }

}
