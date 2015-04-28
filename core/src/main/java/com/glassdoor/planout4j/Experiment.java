package com.glassdoor.planout4j;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.*;

/**
 * Represents an instance of an experiment.
 * An experiment has unique (within its namespace) name, reference to experiment definition,
 * and collection of segments allocated to it.
 * @author ernest.mishkin
 */
public class Experiment {

    public final String name;
    public final String salt;
    public final ExperimentConfig def;
    public final Collection<Integer> usedSegments;

    public Experiment(final String name, final String salt, final ExperimentConfig def, final Collection<Integer> usedSegments) {
        checkArgument(StringUtils.isNotEmpty(name));
        this.name = name;
        checkArgument(StringUtils.isNotEmpty(salt));
        this.salt = salt;
        checkArgument(def != null);
        this.def = def;
        // usedSegments param can be null or empty for default experiment
        this.usedSegments = usedSegments == null ? Collections.<Integer>emptySet() : usedSegments;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof Experiment && name.equals(((Experiment)obj).name);
    }

    @Override
    public String toString() {
        return String.format("exp{%s, %s, %s}", name, usedSegments.size(), def);
    }

}
