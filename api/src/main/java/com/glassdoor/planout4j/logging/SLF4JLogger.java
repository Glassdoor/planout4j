package com.glassdoor.planout4j.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SLF4JLogger extends AbstractJSONLogger {

    public static final String LOGGER_NAME = "planout";
    private static final Logger LOG = LoggerFactory.getLogger(LOGGER_NAME);

    public SLF4JLogger() {}

    public SLF4JLogger(final boolean pretty) {
        super(pretty);
    }

    @Override
    protected void persist(final String record) {
        LOG.info(record);
    }

}
