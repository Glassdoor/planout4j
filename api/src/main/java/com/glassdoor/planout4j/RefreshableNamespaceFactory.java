package com.glassdoor.planout4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Extension of {@link com.glassdoor.planout4j.SimpleNamespaceFactory} capable of refreshing namespaces in thread-safe manner every N minutes.
 */
public class RefreshableNamespaceFactory extends SimpleNamespaceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshableNamespaceFactory.class);
    
    /**
     * This method is called periodically, in order to get the latest namespace configuration.
     */
    @Scheduled(initialDelay = 120_000, fixedDelay = 120_000)
    public void refresh() {
       LOG.info("refreshing ...");
       try {
         namespaceName2namespaceConfigMap = readConfig();
      } catch (Exception e) {
         LOG.error("Namespace refresh failed: Invalid configuration", e);
      }
    }

}
