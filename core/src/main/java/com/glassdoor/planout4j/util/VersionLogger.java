package com.glassdoor.planout4j.util;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs git and maven version info as stored in xxx.git.properties files.
 */
public class VersionLogger {

    private static final Logger LOG = LoggerFactory.getLogger(VersionLogger.class);

    public static Properties properties(final String module) {
        final Properties props = new Properties();
        final String file = "planout4j-" + module + ".git.properties";
        try {
            props.load(VersionLogger.class.getResourceAsStream("/" + file));
        } catch (Exception e) {
            LOG.warn("Failed to load embedded " + file, e);
        }
        return props;
    }

    public static void log(final String module) {
        Properties props = properties(module);
        LOG.info("{} version {} built on {}", module, props.getProperty("git.commit.id.describe"),
                props.getProperty("git.build.time"));
    }

}
