package com.glassdoor.planout4j.config;

import java.util.Map;

/**
 * Formats a config as a string.
 */
public interface ConfigFormatter {

    String format(Map<String, ?> config);

}
