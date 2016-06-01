package com.glassdoor.planout4j.logging;

import com.glassdoor.planout4j.config.ConfFileLoader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException.Missing;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Resolves logging implementation from the master config file, then instantiates and configures it.
 * Falls back on the no-op implementation.
 * @author ernest_mishkin
 */
public final class Planout4jLoggerFactory {

    public static Planout4jLogger getLogger() {
        final Logger log = LoggerFactory.getLogger(Planout4jLoggerFactory.class);
        try {
            final Config config = ConfFileLoader.loadConfig().getConfig("planout4j").getConfig("logging");
            final Planout4jLogger p4jLog = ClassUtils.getClass(config.getString("class")).asSubclass(Planout4jLogger.class).newInstance();
            p4jLog.configure(config);
            log.info("Loaded and configured {} for exposure logging", p4jLog);
            return p4jLog;
        } catch (final Missing e) {
            log.info(e.getMessage());
            return Planout4jLogger.NO_OP;
        } catch (final ClassNotFoundException|IllegalAccessException|InstantiationException e) {
            throw new RuntimeException("Failed to load planout4j logger implementation", e);
        }
    }

    private Planout4jLoggerFactory() {}

}
