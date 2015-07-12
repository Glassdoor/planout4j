package com.glassdoor.planout4j.planout.ops;

import java.util.Map;

import org.json.simple.JSONObject;

public class JSONObjectBuilder extends JSONObject {

    public JSONObjectBuilder() {
    }

    public JSONObjectBuilder(final Map map) {
        super(map);
    }

    @SuppressWarnings("unchecked")
    public JSONObjectBuilder p(String key, Object value) {
        put(key, value);
        return this;
    }

}
