package com.glassdoor.planout4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.glassdoor.planout4j.compiler.PlanoutDSLCompiler;
import com.glassdoor.planout4j.config.Planout4jTestConfigHelper;
import com.glassdoor.planout4j.planout.Interpreter;
import com.glassdoor.planout4j.util.Helper;

import static org.junit.Assert.*;

/**
 * Test ability to evaluate output of DSL compiler (without extra serialize/deserialize steps).
 * We still have to make a deep copy of it b/c javascript-produced objects are immutable and planout code
 * requires some mutations such as adding "salt" key.
 */
@SuppressWarnings("all")
public class CompileAndEvaluateTest {

    @BeforeClass
    public static void before() {
        Planout4jTestConfigHelper.setSystemProperties(false);
    }

    @Test
    public void testCanUseImmutableCompilerOutput() throws Exception {
        Map<String, ?> code = Helper.deepCopy(PlanoutDSLCompiler.dsl_to_json("x = 2;"), null);
        assertEquals(Map.of("x", 2.0), new Interpreter(code, "salt", Collections.EMPTY_MAP, Collections.EMPTY_MAP).getParams());
        code = Helper.deepCopy(PlanoutDSLCompiler.dsl_to_json("x = [1,2];"), null);
        assertEquals(Map.of("x", List.of(1.0, 2.0)), new Interpreter(code, "salt", Collections.EMPTY_MAP, Collections.EMPTY_MAP).getParams());
    }

    @Test
    public void specialValuesTest() throws Exception {
        Map<String, ?> code = Helper.deepCopy(PlanoutDSLCompiler.dsl_to_json(
                "x = \"\"; y = uniformChoice(choices=[\"\"], unit=userid);"), null);
        assertEquals(Map.of("x", "", "y", ""),
                new Interpreter(code, "salt", Map.<String, Object>of("userid", ""), Collections.EMPTY_MAP).getParams());
    }

}
