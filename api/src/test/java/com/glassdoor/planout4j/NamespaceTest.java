package com.glassdoor.planout4j;

import java.io.InputStreamReader;

import org.junit.Test;
import com.google.common.collect.ImmutableMap;

import com.glassdoor.planout4j.compiler.JSONConfigParser;

import static java.lang.String.format;
import static org.junit.Assert.*;

/**
 * Test mapping users to segments, segments to experiments.
 * Make sure the behavior is consistent with respect to ramp-up.
 */
@SuppressWarnings("ALL")
public class NamespaceTest {

    private final int Speed_NR_Ph1_user = 108;
    private final int Speed_NR_Ph2_user = 12345;

    @Test
    public void testPhase1() throws Exception {
        NamespaceConfig nsConf = loadConfig("ns_test1");
        assertEquals(ImmutableMap.of("srJobsResultsPerPage", 15l, "use_pclick", true),
                new Namespace(nsConf, ImmutableMap.of("user_guid", Speed_NR_Ph2_user), null).getParams());
        processPhase1(nsConf);
        assertEquals(ImmutableMap.of("srJobsResultsPerPage", 15l, "use_pclick", true),
                new Namespace(nsConf, ImmutableMap.of("user_guid", Speed_NR_Ph1_user, Namespace.BASELINE_KEY, true), null).getParams());
    }

    @Test
    public void testPhase2() throws Exception {
        NamespaceConfig nsConf = loadConfig("ns_test2");
        assertEquals(nsConf.getTotalSegments(), nsConf.getUsedSegments());
        // all the Speed_NR_Ph1 mappings still hold
        processPhase1(nsConf);
        // additional Speed_NR_Ph2 mappings
        processPhase2(nsConf);
    }

    private void processPhase1(NamespaceConfig nsConf) {
        // no unit in the input map
        try {
            new Namespace(nsConf, ImmutableMap.of("foo", 0), null);
            fail("Expected an error");
        } catch (RuntimeException e) {}
        assertEquals(ImmutableMap.of("srJobsResultsPerPage", 15l, "useParallelSegmentSearch", true, "use_pclick", true),
                ImmutableMap.copyOf(new Namespace(nsConf, ImmutableMap.of("user_guid", Speed_NR_Ph1_user), null).getParams()));
        assertEquals(15, new Namespace(nsConf, ImmutableMap.of("user_guid", Speed_NR_Ph1_user), null).getParam("srJobsResultsPerPage", -1));
        assertEquals(-1, new Namespace(nsConf, ImmutableMap.of("user_guid", Speed_NR_Ph1_user), null).getParam("foo", -1));
        assertTrue(new Namespace(nsConf, ImmutableMap.of("user_guid", Speed_NR_Ph1_user), null).getParam("use_pclick", false));
        // freezing one of the computed params
        assertEquals(ImmutableMap.of("srJobsResultsPerPage", 30l, "useParallelSegmentSearch", true, "use_pclick", true),
                ImmutableMap.copyOf(new Namespace(nsConf, ImmutableMap.of("user_guid", Speed_NR_Ph1_user), ImmutableMap.of("srJobsResultsPerPage", 30l)).getParams()));

    }

    private void processPhase2(NamespaceConfig nsConf) {
        assertEquals(ImmutableMap.of("rating_filter_boxes", "radio", "use_check_boxes_for_job_type_filter", true, "srJobsResultsPerPage", 15l, "use_pclick", true),
                ImmutableMap.copyOf(new Namespace(nsConf, ImmutableMap.of("user_guid", Speed_NR_Ph2_user), null).getParams()));
    }

    private NamespaceConfig loadConfig(String fileName) throws Exception {
        return new JSONConfigParser().parseAndValidate(new InputStreamReader(getClass().getResourceAsStream(format("/namespaces/%s.json", fileName))), null);
    }

}
