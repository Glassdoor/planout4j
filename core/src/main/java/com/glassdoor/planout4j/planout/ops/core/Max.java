package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpCommutative;
import com.glassdoor.planout4j.util.Helper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Max of two or more comparable objects.
 */
public class Max extends PlanOutOpCommutative<Object> {

    public Max(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Object commutativeExecute(final List<Object> values) {
        final List<Comparable> comparables = Helper.cast(values);
        return Collections.max(comparables, Helper.getComparator(values, getClass()));
    }

}
