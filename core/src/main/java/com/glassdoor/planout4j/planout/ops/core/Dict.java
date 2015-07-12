package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpSimple;
import com.glassdoor.planout4j.util.Helper;

/**
 * Corresponds to PlanOut <code>Map</code>.
 * Since internally each operation is a map of its arguments to their subtrees, we just need to deep-copy the map.
 */
public class Dict extends PlanOutOpSimple<Map<String, ?>> {

    public Dict(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Map<String, ?> simpleExecute() {
        final Map<String, ?> copy = Helper.deepCopy(args, null);
        copy.remove("op");
        copy.remove("salt");
        return copy;
    }

}
