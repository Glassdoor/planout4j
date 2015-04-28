package com.glassdoor.planout4j.compiler;

import java.util.Map;

import org.junit.Test;

import com.glassdoor.planout4j.NamespaceConfig;
import com.glassdoor.planout4j.config.NamespaceConfigBuilder;
import com.glassdoor.planout4j.config.ValidationException;

import static java.lang.String.format;
import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
public class NamespaceConfigBuilderTest {

    private static final String[] INVALID_CONFIGS = {"segments_overflow", "duplicate_definition", "duplicate_name",
            "invalid_definition"};

    @Test
    public void testValidConfig() throws Exception {
        NamespaceConfig nsConf = loadConfig("/test_namespace.yaml");
        assertEquals("internal.smoke_test", nsConf.name);
        assertEquals(100, nsConf.getTotalSegments());
        assertEquals("smoke_test", nsConf.salt);
        assertEquals("user_guid", nsConf.unit);
        assertEquals(3, nsConf.getExperimentDefsCount());
        assertEquals(2, nsConf.getActiveExperimentsCount());
        assertEquals(50, nsConf.getUsedSegments());
        assertNotNull(nsConf.getDefaultExperiment());
    }

    @Test
    public void testInvalidConfigs() {
        for (String invalidConfig : INVALID_CONFIGS) {
            try {
                loadConfig(format("/bad_configs/%s.yaml", invalidConfig));
                fail(format("Expected ValidationException while processing %s", invalidConfig));
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }
    }

    private NamespaceConfig loadConfig(String configResource) throws ValidationException {
        System.out.printf("Loading %s\n", configResource);
        return NamespaceConfigBuilder.process((Map) Planout4jConfigParser.createYamlParser()
                .load(getClass().getResourceAsStream(configResource)));
    }
}