package com.glassdoor.planout4j.planout.ops.random;

import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * Flipping a (possibly not fair) coin.
 */
public class BernoulliTrial extends PlanOutOpRandom<Boolean> {

    public BernoulliTrial(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Boolean simpleExecute() {
        final double p = getArgNumeric("p");
        checkState(p >= 0.0 && p <= 1.0, "%s: p must be a number between 0.0 and 1.0, not %s!", getClass(), p);
        final double randVal = getUniform();
        return randVal <= p;
    }

}
