package com.glassdoor.planout4j.planout.ops.core;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpCommutative;
import com.glassdoor.planout4j.util.Helper;
import com.glassdoor.planout4j.planout.ops.utils.Operators;

import java.util.List;
import java.util.Map;

/**
 * Product of two or more numbers.
 */
public class Product extends PlanOutOpCommutative<Number> {

    public Product(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Number commutativeExecute(final List<Object> values) {
        double p = 1.0;
        boolean allIntegers = true;
        for (Object value : values) {
            final Number n = getNumber(value, "multiplier");
            p *= n.doubleValue();
            if (allIntegers && Helper.isRealNumber(n)) {
                allIntegers = false;
            }
            if (p == 0) {
                break;
            }
        }
        return Helper.wrapNumber(p, allIntegers);
    }

    @Override
    public String pretty() {
        return Operators.join((List)Operators.stripArray(getArgList("values")), " * ");
    }

}
