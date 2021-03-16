package com.glassdoor.planout4j.compiler;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Map;

import static org.junit.Assert.*;

public class PlanoutDSLConstructorTest {

    @Test
    public void testTagging() {
        Map map = parse("key: value");
        assertEquals("value", map.get("key"));
        map = parse("key: \n  nestedKey: nestedValue");
        assertEquals(Map.of("nestedKey", "nestedValue"), map.get("key"));
        map = parse("key: |\n  value");
        assertEquals("value", map.get("key"));
        testFailure("key: !planout\n  nestedKey: nestedValue");
        map = parse("key: !planout |\n  x = 2;");
        assertTrue("expecting map", map.get("key") instanceof Map);
        testFailure("key: !planout |\n  value");
    }

    private Map parse(String yaml) {
        return (Map)new Yaml(new PlanoutDSLConstructor()).load(yaml);
    }

    private void testFailure(String yaml) {
        try {
            parse(yaml);
            fail("Expected exception");
        } catch (YAMLException e) {
            e.printStackTrace();
        }
    }

}