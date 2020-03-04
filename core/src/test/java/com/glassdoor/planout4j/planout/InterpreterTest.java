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
    private final Object extendedInterpreterCompiledScript = JSONValue.parse("{\"op\":\"seq\",\"seq\":[{\"op\":\"cond\",\"cond\":[{\"if\":{\"p\":0.2,\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"salt\":\"salty\",\"op\":\"bernoulliTrial\"},\"then\":{\"op\":\"seq\",\"seq\":[{\"op\":\"set\",\"var\":\"explore\",\"value\":true},{\"op\":\"set\",\"var\":\"variant\",\"value\":{\"choices\":{\"op\":\"array\",\"values\":[\"arm1\",\"arm2\",\"arm3\"]},\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"op\":\"uniformChoice\"}}]}},{\"if\":true,\"then\":{\"op\":\"seq\",\"seq\":[{\"op\":\"set\",\"var\":\"score1\",\"value\":{\"value\":{\"values\":[0,{\"op\":\"sum\",\"values\":[{\"op\":\"sum\",\"values\":[12.123,{\"op\":\"product\",\"values\":[-2.1232,{\"op\":\"get\",\"var\":\"feature_1\"}]}]},{\"op\":\"product\",\"values\":[33.122,{\"op\":\"get\",\"var\":\"feature_2\"}]}]}],\"op\":\"max\"},\"op\":\"exp\"}},{\"op\":\"set\",\"var\":\"score2\",\"value\":{\"value\":{\"values\":[0,{\"op\":\"sum\",\"values\":[{\"op\":\"sum\",\"values\":[0.1111,{\"op\":\"product\",\"values\":[0.2222,{\"op\":\"get\",\"var\":\"feature_1\"}]}]},{\"op\":\"product\",\"values\":[0.3333,{\"op\":\"get\",\"var\":\"feature_2\"}]}]}],\"op\":\"max\"},\"op\":\"exp\"}},{\"op\":\"set\",\"var\":\"score3\",\"value\":{\"value\":{\"values\":[0,{\"op\":\"sum\",\"values\":[{\"op\":\"sum\",\"values\":[1,{\"op\":\"product\",\"values\":[5,{\"op\":\"get\",\"var\":\"feature_1\"}]}]},{\"op\":\"product\",\"values\":[-0.6612,{\"op\":\"get\",\"var\":\"feature_2\"}]}]}],\"op\":\"max\"},\"op\":\"exp\"}},{\"op\":\"set\",\"var\":\"explore\",\"value\":false},{\"op\":\"set\",\"var\":\"variant\",\"value\":{\"choices\":{\"op\":\"array\",\"values\":[\"arm1\",\"arm2\",\"arm3\"]},\"weights\":{\"op\":\"array\",\"values\":[{\"op\":\"get\",\"var\":\"score1\"},{\"op\":\"get\",\"var\":\"score2\"},{\"op\":\"get\",\"var\":\"score3\"}]},\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"op\":\"weightedChoice\"}}]}}]}]}");
    private final Map<String, Object> extendedInterpreterInputs = Helper.cast(ImmutableMap.of("feature_1", 1, "feature_2", 0, "userid", 42));


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

    @Test
    public void testExtendedInterpreter() {
        Interpreter proc = new Interpreter(extendedInterpreterCompiledScript, "test_salt", extendedInterpreterInputs, null);
        Map<String, Object> params = proc.getParams();
        assertEquals(false, params.get("explore"));
        assertEquals("arm1", params.get("variant"));
        assertEquals(22022.060942147677, params.get("score1"));
        assertEquals(1.3955659054472516, params.get("score2"));
        assertEquals(403.4287934927351, params.get("score3"));
    }

}