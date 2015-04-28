package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.planout.ops.utils.StopPlanOutException;
import com.glassdoor.planout4j.util.Helper;

/**
 * Explicitly terminates the script.
 */
public class Return extends PlanOutOp<Object> {

    public Return(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public Object execute(final Mapper mapper) {
        // if script calls return; or return();, assume the unit is in the experiment
        final Object value = mapper.evaluate(args.get("value"));
        throw new StopPlanOutException(Helper.asBoolean(value));
    }

}
