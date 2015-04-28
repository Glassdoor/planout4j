package com.glassdoor.planout4j;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.glassdoor.planout4j.compiler.PlanoutDSLCompiler;
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

    @Test
    public void testCanUseImmutableCompilerOutput() throws Exception {
        Map<String, ?> code = Helper.deepCopy(PlanoutDSLCompiler.dsl_to_json("x = 2;"));
        assertEquals(ImmutableMap.of("x", 2.0), new Interpreter(code, "salt", Collections.EMPTY_MAP, Collections.EMPTY_MAP).getParams());
        code = Helper.deepCopy(PlanoutDSLCompiler.dsl_to_json("x = [1,2];"));
        assertEquals(ImmutableMap.of("x", ImmutableList.of(1.0, 2.0)), new Interpreter(code, "salt", Collections.EMPTY_MAP, Collections.EMPTY_MAP).getParams());
    }

    @Test
    public void specialValuesTest() throws Exception {
        Map<String, ?> code = Helper.deepCopy(PlanoutDSLCompiler.dsl_to_json(
                "x = \"\"; y = uniformChoice(choices=[\"\"], unit=userid);"));
        assertEquals(ImmutableMap.of("x", "", "y", ""),
                new Interpreter(code, "salt", ImmutableMap.<String, Object>of("userid", ""), Collections.EMPTY_MAP).getParams());
    }

}
