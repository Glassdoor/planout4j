package com.glassdoor.planout4j.config;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests the {@link Planout4jConfigClasspathBackend} class.
 *
 * @author rschatz
 */
public class Planout4jConfigClasspathBackendTest {

    @Test
    public void testLoadingAll() {
        final Planout4jConfigBackend backend = new Planout4jConfigClasspathBackend() {
            @Override
            protected String getPlanoutSubdirectory() {
                return "classpathbackend";
            }
        };
        final Map<String, String> namespaces = backend.loadAll();
        assertEquals(2, namespaces.size());
        assertTrue(namespaces.containsKey("ns_test1"));
        assertTrue(namespaces.get("ns_test1").contains("experiment_definitions"));
    }
}