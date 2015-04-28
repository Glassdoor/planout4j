package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpBinary;
import com.glassdoor.planout4j.util.Helper;
import com.google.common.collect.ImmutableList;

import java.util.Map;
import java.util.Objects;

/**
 * Sum of two or more numbers.
 */
public abstract class PlanOutOpComparison extends PlanOutOpBinary<Boolean> {

    public PlanOutOpComparison(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Boolean binaryExecute(final Object left, final Object right) {
        final int cmp = Objects.compare(left, right, Helper.getComparator(ImmutableList.of(left, right), getClass()));
        return comparisonExecute(cmp);
    }

    protected abstract boolean comparisonExecute(final int cmpResult);

}
