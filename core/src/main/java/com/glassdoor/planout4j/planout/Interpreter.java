package com.glassdoor.planout4j.planout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.glassdoor.planout4j.planout.ops.utils.Operators;
import com.glassdoor.planout4j.planout.ops.utils.StopPlanOutException;

import static org.apache.commons.collections4.MapUtils.getObject;

/**
 * PlanOut DSL interpreter.
 * This class is <b>not</b> thread-safe and thus cannot be shared between multiple threads.
 */
public class Interpreter extends Mapper {

    private final Assignment env;
    private final Map<String, Object> inputs;

    private final Object serialization;
    private boolean evaluated = false;
    private boolean inExperiment = true;

    public Interpreter(final Object serialization, final String experimentSalt, final Map<String, Object> inputs,
                       final Map<String, Object> overrides)
    {
        super(StringUtils.defaultString(experimentSalt, "global_salt"));
        this.serialization = serialization;
        this.inputs = copyOf(inputs);
        this.env = new Assignment(experimentSalt, overrides);
    }

    /**
     * Get all assigned parameter values from an executed interpreter script.
     * Will lazily evaluate.
     * @return parameter assignments
     */
    public Map<String, Object> getParams() {
        if (!evaluated) {
            try {
                evaluate(serialization);
            } catch (StopPlanOutException e) {
                // StopPlanOutException is raised when script calls "return", which short circuits execution and sets inExperiment
                inExperiment = e.inExperiment;
            }
            evaluated = true;
        }
        return env.getData();
    }

    /**
     * Recursively evaluate PlanOut interpreter code.
     * @param planoutCode parsed JSON
     * @return result of the evaluation
     */
    @Override
    public Object evaluate(final Object planoutCode) {
        // if the object is a PlanOut operator, execute it.
        if (Operators.isOperator(planoutCode)) {
            return Operators.operatorInstance(planoutCode).execute(this);
        }
        // if the object is a list, iterate over the list and evaluate each element
        else if (planoutCode instanceof List) {
            final List<Object> eval = new ArrayList<>();
            for (Object expr : (List)planoutCode) {
                eval.add(evaluate(expr));
            }
            return eval;
        }
        // data is literal
        else {
            return planoutCode;
        }
    }

    @Override
    public boolean has(final String name) {
        return env.has(name);
    }

    @Override
    public Object get(final String name, final Object def) {
        return env.get(name, getObject(inputs, name, def));
    }

    @Override
    public Mapper set(final String name, final Object value) {
        env.set(name, value);
        return this;
    }

    @Override
    public boolean hasOverride(final String name) {
        return env.hasOverride(name);
    }

    public boolean isInExperiment() {
        return inExperiment;
    }

}