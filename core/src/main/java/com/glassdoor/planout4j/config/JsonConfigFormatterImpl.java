package com.glassdoor.planout4j.config;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Formats a config as a json string.
 */
public class JsonConfigFormatterImpl implements ConfigFormatter {

    private final boolean pretty;

    public JsonConfigFormatterImpl() {
        this(false);
    }

    public JsonConfigFormatterImpl(final boolean pretty) {
        this.pretty = pretty;
    }

    public String format(final Map<String, ?> config) {
        return getGson(pretty).toJson(config);
    }

    private static Gson getGson(final boolean pretty) {
        final GsonBuilder builder = new GsonBuilder();
        if (pretty) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }
}
