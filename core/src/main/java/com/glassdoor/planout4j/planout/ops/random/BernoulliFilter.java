package com.glassdoor.planout4j.planout.ops.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * Filtering a list by running Bernoulli trial for each item.
 */
public class BernoulliFilter extends PlanOutOpRandom<List<?>> {

    public BernoulliFilter(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected List<?> simpleExecute() {
        final double p = getArgNumeric("p");
        checkState(p >= 0.0 && p <= 1.0, "%s: p must be a number between 0.0 and 1.0, not %s!", getClass(), p);
        final List<Object> values = getArgList("choices");
        if (values == null || values.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        final List<Object> filtered = new ArrayList<>(values.size());
        for (Object i : values) {
            if (getUniform(0.0, 1.0, i) <= p) {
                filtered.add(i);
            }
        }
        return filtered;
    }
    
}
