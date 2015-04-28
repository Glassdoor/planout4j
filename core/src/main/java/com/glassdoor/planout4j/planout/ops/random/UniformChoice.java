package com.glassdoor.planout4j.planout.ops.random;

import com.glassdoor.planout4j.util.Helper;

import java.util.List;
import java.util.Map;

/**
 * Pick an item from the list with equal probability.
 */
public class UniformChoice<T> extends PlanOutOpRandom<T> {

    public UniformChoice(final Map<String, Object> args) {
        super(args);
    }

    public UniformChoice(final List<T> choices, final Object unit) {
        super(unit);
        args.put("choices", choices);
    }

    @Override
    protected T simpleExecute() {
        final List<T> choices = Helper.cast(getArgList("choices"));
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        int randIndex = (int)(getHash() % choices.size());
        return choices.get(randIndex);
    }
    
}
