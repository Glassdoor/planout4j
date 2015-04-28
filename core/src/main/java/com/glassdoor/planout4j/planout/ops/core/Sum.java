package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpCommutative;
import com.glassdoor.planout4j.util.Helper;
import com.glassdoor.planout4j.planout.ops.utils.Operators;

import java.util.List;
import java.util.Map;

/**
 * Sum of two or more numbers.
 */
public class Sum extends PlanOutOpCommutative<Number> {

    public Sum(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Number commutativeExecute(final List<Object> values) {
        double sum = 0.0;
        boolean allIntegers = true;
        for (Object value : values) {
            final Number n = getNumber(value, "additive");
            sum += n.doubleValue();
            if (allIntegers && Helper.isRealNumber(n)) {
                allIntegers = false;
            }
        }
        return Helper.wrapNumber(sum, allIntegers);
    }

    @Override
    public String pretty() {
        return Operators.join((List)Operators.stripArray(getArgList("values")), " + ");
    }

}
