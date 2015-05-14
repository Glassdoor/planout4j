package com.glassdoor.planout4j.compiler;

import java.io.Reader;
import java.util.Map;

import org.json.simple.JSONValue;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.config.NamespaceConfigBuilder;
import com.glassdoor.planout4j.config.ValidationException;
import com.glassdoor.planout4j.util.Helper;

/**
 * Parses PlanOut4J config represented as JSON with DSL resolved.
 * @author ernest.mishkin
 */
public class JSONConfigParser extends Planout4jConfigParser {

    @Override
    public String getConfigSource() {
        return "JSON";
    }

    @Override
    public NamespaceConfig parseAndValidate(final Reader input, final String namespace) throws ValidationException {
        final Map<String, Object> config = Helper.cast(JSONValue.parse(input));
        return NamespaceConfigBuilder.process(config);
    }

}
