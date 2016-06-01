package com.glassdoor.planout4j.logging;

import java.util.Objects;

import com.google.common.base.Converter;
import com.typesafe.config.Config;


/**
 * Base class for all serializing loggers.
 * @param <S> what to serialize to
 * @author ernest_mishkin
 */
public abstract class AbstractSerializingLogger<S> implements Planout4jLogger {

    private static final String LOG_DEFAULT_EXPOSURE = "log_default_exposure";

    protected final Converter<LogRecord, S> serializer;
    public volatile boolean logDefaultExposure;

    protected AbstractSerializingLogger(final Converter<LogRecord, S> serializer) {
        this.serializer = Objects.requireNonNull(serializer);
    }

    /**
     * Set certain properties from the configuration (<code>logging</code> section of the master planout4j.conf file)
     * @param config logging section config
     */
    @Override
    public void configure(final Config config) {
        logDefaultExposure = config.hasPath(LOG_DEFAULT_EXPOSURE) && config.getBoolean(LOG_DEFAULT_EXPOSURE);
    }

    /**
     * How to persist the serialized record
     * @param record serialized record
     */
    protected abstract void persist(S record);

    @Override
    public void exposed(final LogRecord record) {
        if (logDefaultExposure || record.namespace.getExperiment() != null) {
            persist(serializer.convert(record));
        }
    }

}
