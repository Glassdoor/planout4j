package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;

/**
 * Getting a value of a variable.
 */
public class Get extends PlanOutOp<Object> {

    public Get(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public Object execute(final Mapper mapper) {
        return mapper.get(getArgString("var"));
    }

    @Override
    public String pretty() {
        return getArgString("var");
    }
    
}
