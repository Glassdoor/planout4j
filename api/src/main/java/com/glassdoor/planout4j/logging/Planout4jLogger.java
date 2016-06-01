package com.glassdoor.planout4j.logging;

import com.typesafe.config.Config;


/**
 * Implementations of this interface are responsible for performing exposure logging.
 * If the logging involves more than trivial amount of time/resources, it should be done in a separate thread
 * as planout4j code itself will perform logging serially.
 * @author ernest_mishkin
 */
public interface Planout4jLogger {

    /**
     * Invoked when input (e.g. a user) has been exposed to an experiment within a specific namespace.
     * Any runtime exceptions thrown within implementations will be caught and logged (but will <b>not</b> break the flow).
     * @param record exposure details
     */
    void exposed(LogRecord record);

    /**
     * Set certain properties from the configuration (<code>logging</code> section of the master planout4j.conf file)
     * @param config logging section config
     */
    void configure(Config config);


    /**
     * The "no-op" logger, does nothing. Default one for backwards-compatibility reasons.
     */
    Planout4jLogger NO_OP = new Planout4jLogger() {
        @Override
        public void exposed(final LogRecord record) {}
        @Override
        public void configure(final Config config) {}
    };


}
