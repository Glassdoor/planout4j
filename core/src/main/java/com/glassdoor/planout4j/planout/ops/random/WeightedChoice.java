package com.glassdoor.planout4j.planout.ops.random;

import java.util.List;
import java.util.Map;

import com.glassdoor.planout4j.util.Helper;

import static com.google.common.base.Preconditions.*;

/**
 * Pick an item from the list with probability based on the given weight.
 */
public class WeightedChoice<T> extends PlanOutOpRandom<T> {

    public WeightedChoice(final Map<String, Object> args) {
        super(args);
    }

    public WeightedChoice(final List<T> choices, final List<? extends Number> weights, final Object unit) {
        super(unit);
        args.put("choices", choices);
        args.put("weights", weights);
    }

    @Override
    protected T simpleExecute() {
        final List<T> choices = Helper.cast(getArgList("choices"));
        final List<?> weights = getArgList("weights");
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        checkState(choices.size() == weights.size(),
                "%s: number of choices (%s) is different from number of weights (%s)!",
                getClass(), choices.size(), weights.size());
        final double[] cumWeights = new double[weights.size()];
        double cumSum = 0.0;
        for (int i=0; i < cumWeights.length; i++) {
            final Object weight = weights.get(i);
            cumWeights[i] = getNumber(weight, "weight").doubleValue();
            cumSum += cumWeights[i];
            cumWeights[i] = cumSum;
        }
        final double stopValue = getUniform(0.0, cumSum);
        for (int i=0; i < cumWeights.length; i++) {
            if (stopValue <= cumWeights[i]) {
                return choices.get(i);
            }
        }
        // should never happen
        throw new IllegalStateException("Unexpected: failed to make a weighted choice: " + pretty());
    }
    
}
