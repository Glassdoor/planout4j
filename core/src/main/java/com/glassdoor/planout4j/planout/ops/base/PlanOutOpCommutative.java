package com.glassdoor.planout4j.planout.ops.base;

import com.glassdoor.planout4j.planout.ops.utils.Operators;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Operator that evaluates a list of arguments.
 */
public abstract class PlanOutOpCommutative<T> extends PlanOutOpSimple<T> {

    public PlanOutOpCommutative(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected T simpleExecute() {
        return commutativeExecute(getArgList("values"));
    }

    protected abstract T commutativeExecute(List<Object> values);

    @Override
    public String pretty() {
        final Object values = Operators.stripArray(getArgList("values"));
        String prettyValues;
        if (values instanceof List) {
            prettyValues = Operators.join((List)values);
        } else {
            prettyValues = Operators.pretty(values);
        }
        return format("%s(%s)", getCommutativeString(), prettyValues);
    }

    protected String getCommutativeString() {
        return op();
    }

}
