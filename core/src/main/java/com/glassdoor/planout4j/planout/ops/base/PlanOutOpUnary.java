package com.glassdoor.planout4j.planout.ops.base;

import com.glassdoor.planout4j.planout.ops.utils.Operators;

import java.util.Map;

/**
 * Represents a unary operator.
 */
public abstract class PlanOutOpUnary<T> extends PlanOutOpSimple<T> {

    public PlanOutOpUnary(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected T simpleExecute() {
        return unaryExecute(getArgMixed("value"));
    }

    protected abstract T unaryExecute(final Object value);

    @Override
    public String pretty() {
        return getUnaryString() + Operators.pretty(getArgMixed("value"));
    }

    protected String getUnaryString() {
        return op();
    }

}
