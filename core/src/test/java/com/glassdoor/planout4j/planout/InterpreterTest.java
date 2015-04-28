package com.glassdoor.planout4j.planout;

import java.util.Map;

import org.json.simple.JSONValue;
import org.junit.Test;
import com.google.common.collect.ImmutableMap;

import com.glassdoor.planout4j.util.Helper;

import static org.junit.Assert.*;

public class InterpreterTest {

    private final Object compiled = JSONValue.parse("{\"op\":\"seq\",\"seq\":[{\"op\":\"set\",\"var\":\"group_size\",\"value\":{\"choices\":{\"op\":\"array\",\"values\":[1,10]},\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"op\":\"uniformChoice\"}},{\"op\":\"set\",\"var\":\"specific_goal\",\"value\":{\"p\":0.8,\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"op\":\"bernoulliTrial\"}},{\"op\":\"cond\",\"cond\":[{\"if\":{\"op\":\"get\",\"var\":\"specific_goal\"},\"then\":{\"op\":\"seq\",\"seq\":[{\"op\":\"set\",\"var\":\"ratings_per_user_goal\",\"value\":{\"choices\":{\"op\":\"array\",\"values\":[8,16,32,64]},\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"op\":\"uniformChoice\"}},{\"op\":\"set\",\"var\":\"ratings_goal\",\"value\":{\"op\":\"product\",\"values\":[{\"op\":\"get\",\"var\":\"group_size\"},{\"op\":\"get\",\"var\":\"ratings_per_user_goal\"}]}}]}}]}]}");
    private final String interpreterSalt = "foo";
    private final Map<String, Object> inputs = Helper.cast(ImmutableMap.of("userid", 123454));

    @Test
    public void testInterpreter() {
        Interpreter proc = new Interpreter(compiled, interpreterSalt, inputs, null);
        Map<String, Object> params = proc.getParams();
        assertEquals(true, params.get("specific_goal"));
        assertEquals(320l, params.get("ratings_goal"));
    }

    @Test
    public void testInterpreterOverrides() {
        // test overriding a parameter that gets set by the experiment
        Map<String, Object> overrides = Helper.cast(ImmutableMap.of("specific_goal", false));
        Interpreter proc = new Interpreter(compiled, interpreterSalt, inputs, overrides);
        assertEquals(false, proc.getParams().get("specific_goal"));
        assertNull(proc.getParams().get("ratings_goal"));
        // test to make sure input data can also be overridden
        Map<String, Object> wrongInputs = Helper.cast(ImmutableMap.of("userid", 123453));
        overrides = Helper.cast(ImmutableMap.of("userid", 123454));
        proc = new Interpreter(compiled, interpreterSalt, wrongInputs, overrides);
        assertEquals(true, proc.getParams().get("specific_goal"));
    }

}