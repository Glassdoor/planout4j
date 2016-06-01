package com.glassdoor.planout4j.logging;

import java.util.Map;


@SuppressWarnings("unused")
class JSONLoggingRecord {

    String event;
    String timestamp;
    String namespace;
    String experiment;
    String salt;
    String checksum;
    Map inputs;
    Map overrides;
    Map params;

    public String getEvent() {
        return event;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getExperiment() {
        return experiment;
    }

    public String getSalt() {
        return salt;
    }

    public String getChecksum() {
        return checksum;
    }

    public Map getInputs() {
        return inputs;
    }

    public Map getOverrides() {
        return overrides;
    }

    public Map getParams() {
        return params;
    }

}
