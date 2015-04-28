package com.glassdoor.planout4j.planout.ops.random;

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * Random selection without replacement.
 */
public class Sample<E> extends PlanOutOpRandom<List<E>> {

    public Sample(final Map<String, Object> args) {
        super(args);
    }

    public Sample(final List<E> choices, final int draws, final Object unit) {
        super(unit);
        args.put("choices", choices);
        args.put("draws", draws);
    }

    // implements Fisher-Yates shuffle
    @Override
    protected List<E> simpleExecute() {
        // copy the list of choices so that we don't mutate it
        @SuppressWarnings("unchecked")
        final List<E> choices = new ArrayList<>(ObjectUtils.defaultIfNull(getArgList("choices"), Collections.EMPTY_LIST));
        int numDraws;
        if (hasArg("draws")) {
            numDraws = getArgInt("draws");
            checkState(numDraws <= choices.size(), "%s: cannot make %s draws when only %s choices are available",
                    getClass(), numDraws, choices.size());
            checkState(numDraws >= 0, "%s: 'draws' cannot be negative (%s)", getClass(), numDraws);
        } else {
            numDraws = choices.size();
        }
        for (int i = choices.size() - 1; i >= 0; i--) {
            int j = (int) (getHash(i) % (i + 1));
            Collections.swap(choices, i, j);
        }
        return choices.subList(0, numDraws);
    }

}
