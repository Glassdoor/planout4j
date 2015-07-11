package com.glassdoor.planout4j.planout.ops.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.utils.Operators;
import com.glassdoor.planout4j.util.Helper;

import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;

/**
 * Abstract base class for PlanOut operations.
 * @param <T> expected evaluation type
 */
public abstract class PlanOutOp<T> {

    protected static final Logger LOG = LoggerFactory.getLogger(PlanOutOp.class);

    protected final Map<String, Object> args;

    /**
     * All PlanOut operators have some set of args that act as required and optional arguments.
     * @param args operator's JSON structure (a map)
     */
    protected PlanOutOp(Map<String, Object> args) {
        checkArgument(args != null, "args is null");
        this.args = args;
    }

    /**
     * This is used for testing purposes.
     */
    protected PlanOutOp() {
        args = new HashMap<>();
        args.put("op", Operators.operators.inverse().get(getClass()));

    }

    /**
     * All PlanOut operators must implement execute.
     * @param mapper instance of Interpreter evaluating the parser tree
     * @return result of applying the operator to its arguments
     */
    protected abstract T execute(final Mapper mapper);

    public T executeWithLogging(final Mapper mapper) {
        final T res = execute(mapper);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Result of evaluating {} is {}", pretty(), res);
        }
        return res;
    }

    public String prettyArgs() {
        return Operators.prettyParamFormat(args);
    }

    public String pretty() {
        return format("%s(%s)", op(), prettyArgs());
    }

    public Object getArgMixed(final String name) {
        final Object arg = args.get(name);
        checkState(arg != null, "%s: missing argument: %s.", getClass(), name);
        return arg;
    }

    public Number getNumber(final Object o, final String name) {
        checkState(o instanceof Number, "%s: %s must be a number but is %s", getClass(), name, Helper.getClassName(o));
        return (Number)o;
    }

    public int getArgInt(final String name) {
        final double d = getArgFloat(name);
        final int i = (int)d;
        checkState(i == d, "%s: %s must be an integer but is %s.", getClass(), name, d);
        return i;
    }

    public double getArgFloat(final String name) {
        final Object arg = getArgMixed(name);
        checkState(arg instanceof Number, "%s: %s must be a number but is %s.", getClass(), name, arg);
        return ((Number)arg).doubleValue();
    }

    public String getArgString(final String name) {
        final Object arg = getArgMixed(name);
        checkState(arg instanceof String, "%s: %s must be a string.", getClass(), name);
        return arg.toString();
    }

    public double getArgNumeric(final String name) {
        return getArgFloat(name);
    }

    public List<Object> getArgList(final String name) {
        final Object arg = getArgMixed(name);
        checkState(arg instanceof List, "%s: %s must be a list.", getClass(), name);
        return Helper.cast(arg);
    }

    public Map<String, Object> getArgMap(final String name) {
        final Object arg = getArgMixed(name);
        checkState(arg instanceof Map, "%s: %s must be a map.", getClass(), name);
        return Helper.cast(arg);
    }

    public Object getArgIndexish(final String name) {
        final Object arg = getArgMixed(name);
        checkState(arg instanceof Map || arg instanceof List, "%s: %s must be a map or a list.", getClass(), name);
        return arg;
    }

    public boolean hasArg(final String name) {
        return args.containsKey(name);
    }

    public PlanOutOp setArg(final String name, final Object value) {
        args.put(name, value);
        return this;
    }

    public PlanOutOp setSalt(final Object salt) {
        return setArg("salt", salt);
    }

    public PlanOutOp setFullSalt(final Object salt) {
        return setArg("full_salt", salt);
    }

    protected String op() {
        return args.get("op").toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + args.size() + "]";
    }

    @Override
    public int hashCode() {
        return args.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        return args.equals(((PlanOutOp)obj).args);
    }

}
