package com.glassdoor.planout4j.planout.ops.random;

import java.util.Map;

/**
 * Random integer in a given interval.
 */
public class RandomInteger extends PlanOutOpRandom<Long> {

    public RandomInteger(final Map<String, Object> args) {
        super(args);
    }

    public RandomInteger(final long min, final long max, final Object unit) {
        super(unit);
        args.put("min", min);
        args.put("max", max);
    }

    /**
     * @return  long value in the range [min .. max]
     */
    @Override
    protected Long simpleExecute() {
        final long minVal = getArgInt("min");
        final long maxVal = getArgInt("max");
        return minVal + getHash() % (maxVal - minVal + 1);
    }
}
