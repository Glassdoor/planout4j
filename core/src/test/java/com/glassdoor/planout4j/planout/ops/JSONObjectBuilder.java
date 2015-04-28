package com.glassdoor.planout4j.planout.ops;

import org.json.simple.JSONObject;

public class JSONObjectBuilder extends JSONObject {

    @SuppressWarnings("unchecked")
    public JSONObjectBuilder p(String key, Object value) {
        put(key, value);
        return this;
    }

}
