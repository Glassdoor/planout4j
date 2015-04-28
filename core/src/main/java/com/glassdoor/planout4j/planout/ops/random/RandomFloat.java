package com.glassdoor.planout4j.planout.ops.random;

import java.util.Map;

/**
 * Random real number between 0 and 1.
 */
public class RandomFloat extends PlanOutOpRandom<Double> {

    public RandomFloat(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Double simpleExecute() {
        final double minVal = getArgFloat("min");
        final double maxVal = getArgFloat("max");
        return getUniform(minVal, maxVal);
    }
}
