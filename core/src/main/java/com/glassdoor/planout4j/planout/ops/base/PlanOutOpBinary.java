package com.glassdoor.planout4j.planout.ops.base;

import com.glassdoor.planout4j.planout.ops.utils.Operators;

import java.util.Map;

import static java.lang.String.format;

/**
 * Represents a binary operator.
 */
public abstract class PlanOutOpBinary<T> extends PlanOutOpSimple<T> {

    public PlanOutOpBinary(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected T simpleExecute() {
        return binaryExecute(getArgMixed("left"), getArgMixed("right"));
    }

    protected abstract T binaryExecute(final Object left, final Object right);

    @Override
    public String pretty() {
        return format("%s %s %s",
                Operators.pretty(getArgMixed("left")),
                getInfixString(),
                Operators.pretty(getArgMixed("right")));
    }

    protected String getInfixString() {
        return op();
    }

}
