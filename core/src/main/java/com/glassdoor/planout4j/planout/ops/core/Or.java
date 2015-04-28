package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.util.Helper;
import com.glassdoor.planout4j.planout.ops.utils.Operators;

import java.util.Map;

/**
 * Logical OR, short-circuit.
 */
public class Or extends PlanOutOp<Boolean> {

    public Or(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public Boolean execute(final Mapper mapper) {
        for (Object clause : getArgList("values")) {
            if (Helper.asBoolean(mapper.evaluate(clause))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String pretty() {
        return Operators.join(getArgList("values"), "||");
    }

}
