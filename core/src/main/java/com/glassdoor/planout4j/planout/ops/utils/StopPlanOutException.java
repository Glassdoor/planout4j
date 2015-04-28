package com.glassdoor.planout4j.planout.ops.utils;

/**
 * Exception that gets raised when "return" op is evaluated.
 */
public class StopPlanOutException extends RuntimeException {

    public final boolean inExperiment;

    public StopPlanOutException(final boolean inExperiment) {
        this.inExperiment = inExperiment;
    }

}
