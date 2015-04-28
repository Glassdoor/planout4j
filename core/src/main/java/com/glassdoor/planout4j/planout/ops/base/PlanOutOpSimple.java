package com.glassdoor.planout4j.planout.ops.base;

import com.glassdoor.planout4j.planout.Mapper;

import java.util.Map;

/**
 * PlanOutOpSimple is the easiest way to implement simple operators.
 * The class automatically evaluates the values of all args passed in via execute(),
 * and stores the PlanOut mapper object and evaluated args as instance variables.
 * The user can then extend PlanOutOpSimple and implement simpleExecute().
 */
public abstract class PlanOutOpSimple<T> extends PlanOutOp<T> {

    protected Mapper mapper;

    protected PlanOutOpSimple(Map<String, Object> args) {
        super(args);
    }

    protected PlanOutOpSimple() {}

    @Override
    public T execute(final Mapper mapper) {
        // TODO: make sure PlanOutOpSimple instances are NOT reused b/c the way Interpreter instance is maintained as object state is not thread-safe!
        this.mapper = mapper;
        for (String param : args.keySet()) {
            args.put(param, mapper.evaluate(args.get(param)));
        }
        return simpleExecute();
    }

    protected abstract T simpleExecute();

}
