package com.glassdoor.planout4j.logging;

import java.util.Map;

import com.glassdoor.planout4j.Namespace;
import com.glassdoor.planout4j.NamespaceConfig;
import org.json.simple.JSONValue;
import org.junit.Test;


@SuppressWarnings("ALL")
public class JSONSerializerTest {

    private static final Object TEST_DEF = JSONValue.parse("{\"op\":\"seq\",\"seq\":[{\"op\":\"set\",\"var\":\"group_size\",\"value\":{\"choices\":{\"op\":\"array\",\"values\":[1,10]},\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"op\":\"uniformChoice\"}},{\"op\":\"set\",\"var\":\"specific_goal\",\"value\":{\"p\":0.8,\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"op\":\"bernoulliTrial\"}},{\"op\":\"cond\",\"cond\":[{\"if\":{\"op\":\"get\",\"var\":\"specific_goal\"},\"then\":{\"op\":\"seq\",\"seq\":[{\"op\":\"set\",\"var\":\"ratings_per_user_goal\",\"value\":{\"choices\":{\"op\":\"array\",\"values\":[8,16,32,64]},\"unit\":{\"op\":\"get\",\"var\":\"userid\"},\"op\":\"uniformChoice\"}},{\"op\":\"set\",\"var\":\"ratings_goal\",\"value\":{\"op\":\"product\",\"values\":[{\"op\":\"get\",\"var\":\"group_size\"},{\"op\":\"get\",\"var\":\"ratings_per_user_goal\"}]}}]}}]}]}");

    @Test
    public void testSerialization() {
        final JSONSerializer ser = new JSONSerializer();
        final NamespaceConfig nsConf = new NamespaceConfig("test_ns", 100, "userid", null);
        nsConf.defineExperiment("def", (Map<String, ?>)TEST_DEF);
        nsConf.setDefaultExperiment("def");
        nsConf.addExperiment("def_exp", "def", 100);
        nsConf.noMoreChanges();
        final Map<String, ?> input = Map.of("userid", 12345);
        final Namespace ns = new Namespace(nsConf, input, null);
        System.out.println(ser.doForward(new LogRecord(ns, input, null)));
    }


}
