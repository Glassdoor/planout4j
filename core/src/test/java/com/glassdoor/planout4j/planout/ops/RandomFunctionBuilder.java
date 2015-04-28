package com.glassdoor.planout4j.planout.ops;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.planout.ops.random.PlanOutOpRandom;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * Helper class used in evaluating random operators.
 */
class RandomFunctionBuilder {

    final Object experimentSalt;
    final Class<? extends PlanOutOpRandom> randomOpClass;
    final Map<String, ?> args;

    RandomFunctionBuilder(Object experimentSalt, Class<? extends PlanOutOpRandom> randomOpClass, Map<String, ?> args) {
        this.experimentSalt = experimentSalt;
        this.randomOpClass = randomOpClass;
        this.args = args;
    }

    PlanOutOp getRandom(Object unit) {
        try {
            return randomOpClass.getConstructor(Map.class).newInstance(new HashMap<>(args)).setArg("unit", unit);
        } catch (Exception e) {
            throw new RuntimeException(format("Failed to create an instance of %s", randomOpClass), e);
        }
    }

}
