package com.glassdoor.planout4j.planout.ops.core;

import java.util.List;
import java.util.Map;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.planout.ops.utils.Operators;

import static java.lang.String.format;

/**
 * Returns the first element of an array that evaluates to a not null value.
 */
public class Coalesce extends PlanOutOp<Object> {

    public Coalesce(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public Object execute(final Mapper mapper) {
        for (Object value : getArgList("values")) {
            final Object eval = mapper.evaluate(value);
            if (eval != null) {
                return eval;
            }
        }
        return null;
    }

    @Override
    public String pretty() {
        final Object values = Operators.stripArray(getArgMixed("values"));
        return format("coalesce(%s)", Operators.join((List)values));
    }

}
