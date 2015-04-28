package com.glassdoor.planout4j.planout.ops;

import org.json.simple.JSONArray;

public class JSONArrayBuilder extends JSONArray {

    @SuppressWarnings("unchecked")
    public JSONArrayBuilder a(Object value) {
        add(value);
        return this;
    }
}
