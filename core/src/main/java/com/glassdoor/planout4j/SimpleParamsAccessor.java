package com.glassdoor.planout4j;

import java.util.Map;

import com.google.common.collect.ImmutableMap;


public class SimpleParamsAccessor extends ParamsAccessor {

    private final Map<String, ?> params;

    public SimpleParamsAccessor(final Map<String, ?> params) {
        this.params = ImmutableMap.copyOf(params);
    }

    @Override
    protected Map<String, ?> getParams() {
        return params;
    }

}
