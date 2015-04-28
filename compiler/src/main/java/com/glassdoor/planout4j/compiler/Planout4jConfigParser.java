package com.glassdoor.planout4j.compiler;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.config.KeyStrings;
import com.glassdoor.planout4j.config.NamespaceConfigBuilder;
import com.glassdoor.planout4j.config.ValidationException;
import com.glassdoor.planout4j.util.Helper;

/**
 * Helper/utility class for parsing Planout4j config expressed as either YAML or JSON.
 * This class carries no state and all methods are static.
 * @author ernest.mishkin
 */
public class Planout4jConfigParser {
   
    private static final Logger LOG = LoggerFactory.getLogger(Planout4jConfigParser.class);

    /**
     * @return YAML parser set to automatically compile PlanOut DSL (annotated with <code>!planout</code> tag)
     */
    public static Yaml createYamlParser() {
        return new Yaml(new PlanoutDSLConstructor());
    }

    /**
     * Parses YAML and returns the raw Map/Lists structure.
     * {@link com.glassdoor.planout4j.NamespaceConfig} is built behind the scenes which covers the validation.
     * @param yaml YAML data representing Namespace
     * @return Map parsed and validated namespace configuration
     * @throws ValidationException in case of any problem with the configuration
     */
    public static Map<String, ?> parseAndValidateYAML(final Reader yaml) throws ValidationException {
        final Map<String, Object> config = Helper.cast(createYamlParser().load(yaml));
        NamespaceConfigBuilder.process(config);
        return config;
    }

    /**
     * Parses JSON and returns Namespace configuration.
     * @param json JSON data representing Namespace
     * @return NamespaceConfig parsed and validated namespace configuration
     * @throws ValidationException in case of any problem with the configuration
     */
    public static NamespaceConfig parseAndValidateJSON(final Reader json) throws ValidationException {
        final Map<String, Object> config = Helper.cast(JSONValue.parse(json));
        return NamespaceConfigBuilder.process(config);
    }
    
    /**
     * Parses JSON and returns Namespace configuration for all elements in the collection.
     * @param namespace2Json namespace2Json a map of namespace names to corresponding json configs
     * @return a map namespace names to NamespaceConfig objects
     * @throws ValidationException if any json is not valid
     */
    public static Map<String, NamespaceConfig> parseAndValidateJSON(Map<String, String> namespace2Json) throws ValidationException{
       List<String> invalidNamespaces = new ArrayList<>();
       Map<String, NamespaceConfig> namespace2ConfigObj = new HashMap<>();
       for(String namespace : namespace2Json.keySet()){
          try{
             namespace2ConfigObj.put(namespace, parseAndValidateJSON(new StringReader(namespace2Json.get(namespace))));
          }catch(Exception e){
             LOG.error("Error while parsing and validating json for " + namespace, e);
             invalidNamespaces.add(namespace);
          }
       }
       
       if(invalidNamespaces.isEmpty()){
          return namespace2ConfigObj;
       }else{
          throw new ValidationException("Unable to parse and validate namespace jsons for " + invalidNamespaces);
       }
    }
    
    /**
     * Inserts namespace name into the yaml 
     * @param yaml
     * @param namespaceName
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String insertNamespaceNameIntoYaml(String yaml, String namespaceName){
        Yaml yamlParser = createYamlParser();
        final Map<String, Object> config = Helper.cast(yamlParser.load(yaml));
        ((Map)config.get(KeyStrings.NAMESPACE)).put(KeyStrings.NAME, namespaceName);
        return yamlParser.dump(config);
    }

    private Planout4jConfigParser() {}

}
