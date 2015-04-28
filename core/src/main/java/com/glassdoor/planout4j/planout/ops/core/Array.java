package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.planout.ops.utils.Operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A sequence (aka array, aka list).
 */
public class Array extends PlanOutOp<List<?>> {

    public Array(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public List<?> execute(final Mapper mapper) {
        final List<Object> eval = new ArrayList<>();
        for (Object value : getArgList("values")) {
            eval.add(mapper.evaluate(value));
        }
        return eval;
    }

    @Override
    public String pretty() {
        return Operators.pretty(getArgMixed("values"));
    }

}
