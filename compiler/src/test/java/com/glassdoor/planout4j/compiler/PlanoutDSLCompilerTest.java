package com.glassdoor.planout4j.compiler;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.glassdoor.planout4j.util.Helper;

import static org.junit.Assert.*;

public class PlanoutDSLCompilerTest {

    @Test
    public void test_dsl_to_json() throws Exception {
        Map<String, ?>  tests = Map.of(
                "x = 2;",
                Map.of("op", "set", "var", "x", "value", 2.0),
                "return bernoulliTrial(0.1)",
                Map.of("op", "return", "value", Map.of("value", 0.1, "op", "bernoulliTrial"))
        );
        for (String input : tests.keySet()) {
            Map output = PlanoutDSLCompiler.dsl_to_json(input);
            assertEquals("seq", output.get("op"));
            List seq = Helper.cast(output.get("seq"));
            assertEquals(1, seq.size());
            assertEquals(tests.get(input), seq.get(0));
        }
    }

}