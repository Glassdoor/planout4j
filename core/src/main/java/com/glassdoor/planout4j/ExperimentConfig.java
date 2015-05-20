package com.glassdoor.planout4j;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.glassdoor.planout4j.util.Helper;

import static com.google.common.base.Preconditions.*;

/**
 * Experiment config (aka experiment definition) has a unique (within its namespace) name (referred to as "definition")
 * and the script (aka "assign") representing compiled PlanOut DSL instructions to be interpreted at runtime.
 * @author ernest.mishkin
 */
public class ExperimentConfig {

    public final String definition;
    private final Map<String, ?> script;

    public ExperimentConfig(final String definition, final Map<String, ?> script) {
        checkArgument(StringUtils.isNotEmpty(definition));
        this.definition = definition;
        checkArgument(MapUtils.isNotEmpty(script));
        this.script = script;
    }

    public final Map<String, ?> getCopyOfScript() {
        return Helper.deepCopy(script, null);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this ||
                obj instanceof ExperimentConfig && definition.equals(((ExperimentConfig)obj).definition);
    }

    @Override
    public String toString() {
        return String.format("exp_def[%s]", definition);
    }

}
