package com.glassdoor.planout4j;

import java.util.Map;


/**
 * A mechanism to access the (output) parameters.
 * A {@link Namespace} is the primary concrete implementation, but a fixed data structure supplier can be used in testing.
 * @author ernest.mishkin
 */
public abstract class ParamsAccessor {

    protected abstract Map<String, ?> getParams();

    public int getParam(final String key, final int def) {
        return getParams().containsKey(key) ? ((Number)getParams().get(key)).intValue() : def;
    }

    public float getParam(final String key, final float def) {
        return getParams().containsKey(key) ? ((Number)getParams().get(key)).floatValue() : def;
    }

    public boolean getParam(final String key, final boolean def) {
        return getParams().containsKey(key) ? ((Boolean)getParams().get(key)) : def;
    }

    public String getParam(final String key, final String def) {
        return getParams().containsKey(key) ? ((String)getParams().get(key)) : def;
    }

}
