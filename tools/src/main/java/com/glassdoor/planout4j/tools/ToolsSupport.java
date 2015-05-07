package com.glassdoor.planout4j.tools;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;

class ToolsSupport {

    static void initLogging(final String toolDesc) {
        PropertyConfigurator.configure(Thread.currentThread().getContextClassLoader().getResource("planout4j-tools-log4j.properties"));
        LoggerFactory.getLogger("planout4j.tools").info("Starting " + toolDesc);
    }

}
