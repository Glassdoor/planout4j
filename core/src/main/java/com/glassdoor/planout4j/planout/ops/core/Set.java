package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.planout.ops.utils.Operators;

import static java.lang.String.format;

/**
 * Getting a value of a variable.
 */
public class Set extends PlanOutOp<Object> {

    public Set(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public Object execute(final Mapper mapper) {
        final String var = getArgString("var");
        final Object value = getArgMixed("value");
        if (mapper.hasOverride(var)) {
            return null;
        }
        // if the value is operator, add the name of the variable as a salt if no salt is provided.
        if (Operators.isOperator(value) && Operators.get(value, "salt") == null) {
            Operators.set(value, "salt", var);
        }
        // if we are setting the special variable, experiment_salt, update mapper object accordingly with the new experiment-level salt
        if (var.equals("experiment_salt")) {
            mapper.setExperimentSalt(value.toString());
        }
        // finally store the value in env
        mapper.set(var, mapper.evaluate(value));
        return null;
    }

    @Override
    public String pretty() {
        return format("%s = %s", getArgString("var"), Operators.pretty(getArgMixed("value")));
    }
    
}
