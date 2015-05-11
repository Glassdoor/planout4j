package com.glassdoor.planout4j.compiler;

import java.io.Reader;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.config.KeyStrings;
import com.glassdoor.planout4j.config.NamespaceConfigBuilder;
import com.glassdoor.planout4j.config.ValidationException;
import com.glassdoor.planout4j.util.Helper;

/**
 * Parses PlanOut4J config represented as YAML with embedded PlanOut DSL.
 * @author ernest.mishkin
 */
public class YAMLConfigParser extends Planout4jConfigParser {

    /**
     * @return YAML parser set to automatically compile PlanOut DSL (annotated with <code>!planout</code> tag)
     */
    public static Yaml createYamlParser() {
        return new Yaml(new PlanoutDSLConstructor());
    }

    @Override
    public String getConfigSource() {
        return "YAML";
    }

    @Override
    public NamespaceConfig parseAndValidate(final Reader input, final String namespaceName) throws ValidationException {
        final Map<String, Object> config = Helper.cast(createYamlParser().load(input));
        final Map<String, Object> namespaceSection = Helper.cast(config.get(KeyStrings.NAMESPACE));
        if (StringUtils.isEmpty(namespaceName) && !namespaceSection.containsKey(KeyStrings.NAME)) {
            throw new ValidationException("Config doesn't have name and no namespace name is explicitly provided");
        }
        if (StringUtils.isNotEmpty(namespaceName)) {
            namespaceSection.put(KeyStrings.NAME, namespaceName);
        }
        return NamespaceConfigBuilder.process(config);
    }

}
