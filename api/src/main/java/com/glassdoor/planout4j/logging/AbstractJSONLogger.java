package com.glassdoor.planout4j.logging;

public abstract class AbstractJSONLogger extends AbstractSerializingLogger<String> {

    protected AbstractJSONLogger() {
        this(false);
    }

    protected AbstractJSONLogger(final boolean pretty) {
        super(new JSONSerializer(pretty));
    }

}
