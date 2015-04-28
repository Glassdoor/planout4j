package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.planout.ops.utils.Operators;

/**
 * A sequence (aka array, aka list).
 */
public class Seq extends PlanOutOp<Object> {

    public Seq(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public Object execute(final Mapper mapper) {
        for (Object op : getArgList("seq")) {
            mapper.evaluate(op);
        }
        return null;
    }

    @Override
    public String pretty() {
        return Operators.join(getArgList("seq")).replaceAll(", ", "\n");
    }

}
