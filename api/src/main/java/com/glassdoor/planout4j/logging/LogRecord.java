package com.glassdoor.planout4j.logging;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.glassdoor.planout4j.Namespace;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;


/**
 * Represents data logged at exposure.
 * @author ernest_mishkin
 */
public class LogRecord {

    public final Namespace namespace;
    public final Map<String, ?> input;
    public final Map<String, ?> overrides;

    public LogRecord(final Namespace namespace, final Map<String, ?> input, final Map<String, ?> overrides) {
        this.namespace = namespace;
        this.input = ImmutableMap.copyOf(input);
        //noinspection unchecked,CollectionsFieldAccessReplaceableByMethodCall
        this.overrides = overrides == null ? Collections.EMPTY_MAP : ImmutableMap.copyOf(overrides);
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LogRecord record = (LogRecord) o;
        return Objects.equals(namespace, record.namespace) &&
               Objects.equals(input, record.input) &&
               Objects.equals(overrides, record.overrides);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, input, overrides);
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("namespace", namespace.getName())
                          .add("experiment", namespace.getExperiment().name)
                          .add("input", input)
                          .add("overrides", overrides)
                          .toString();
    }

}
