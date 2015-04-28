package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;

/**
 * A literal (constant).
 */
public class Literal extends PlanOutOp<Object> {

    public Literal(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public Object execute(final Mapper mapper) {
        return getArgMixed("value");
    }

    @Override
    public String pretty() {
        return String.valueOf(getArgMixed("value"));
    }

}
