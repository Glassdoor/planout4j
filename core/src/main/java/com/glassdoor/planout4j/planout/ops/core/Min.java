package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpCommutative;
import com.glassdoor.planout4j.util.Helper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Min of two or more comparable objects.
 */
public class Min extends PlanOutOpCommutative<Object> {

    public Min(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Object commutativeExecute(final List<Object> values) {
        final List<Comparable> comparables = Helper.cast(values);
        return Collections.min(comparables, Helper.getComparator(values, getClass()));
    }

}
