package com.glassdoor.planout4j.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Responsible for loading PlanOut4J config file.
 * Checks system property <code>planout4jConfigFile</code> and defaults to the embedded <code>planout4j.conf</code>
 * (which, of course, still allows for individual properties to be overridden via system props).
 */
public class ConfFileLoader {

    /**
     * Returns config object which will attempt to resolve properties in the following order:<ol>
     *     <li>custom config file pointed to by <code>planout4jConfigFile</code> system property</li>
     *     <li>default embedded config</li>
     * </ol>
     * @return Planout4j configuration (backends, etc.)
     */
    public static Config loadConfig() {
        final Config internalConfig = ConfigFactory.load("planout4j");
        final String customConfigPath = System.getProperty("planout4jConfigFile");
        final Logger log = LoggerFactory.getLogger(ConfFileLoader.class);
        if (customConfigPath != null) {
            final File configFile = new File(customConfigPath);
            if (configFile.isFile() && configFile.canRead()) {
                log.info("Using custom config: {}", configFile.getAbsolutePath());
                return ConfigFactory.parseFile(configFile).withFallback(internalConfig);
            } else {
                log.warn("Invalid custom config path: {} (resolves to {})", customConfigPath, configFile.getAbsolutePath());
            }
        }
        log.info("Using embdded default config");
        return internalConfig;
    }

}
