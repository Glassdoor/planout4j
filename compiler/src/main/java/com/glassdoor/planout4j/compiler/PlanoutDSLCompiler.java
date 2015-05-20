package com.glassdoor.planout4j.compiler;

import java.io.IOException;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.glassdoor.planout4j.config.ValidationException;
import com.glassdoor.planout4j.util.Helper;

import static com.google.common.base.Preconditions.*;

/**
 * Utilizes JavaScript-based PlanOut DSL to JSON compiler.
 * This class carries no mutable state and the main method is static.
 * Under the hood, a new {@link javax.script.ScriptEngine} instance is created every time, so the class is thread-safe.
 * @author ernest.mishkin
 */
public class PlanoutDSLCompiler {

    private static final String SCRIPT_RESOURCE = "/planout.js";

    private static final String script;

    static  {
        try {
            script = Resources.asCharSource(PlanoutDSLCompiler.class.getResource(SCRIPT_RESOURCE), Charsets.UTF_8).read();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read classpath resource " + SCRIPT_RESOURCE, e);
        }
    }

    /**
     * Compile DSL to JSON.
     * @param dsl input string in PlanOut DSL
     * @return JSON object corresponding to the parse tree
     * @throws com.glassdoor.planout4j.config.ValidationException in case there's a compilation problem
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> dsl_to_json(final String dsl) throws ValidationException {
        checkArgument(StringUtils.isNotEmpty(dsl), "dsl text is null or empty");
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        engine.put("input", dsl);
        try {
            engine.eval(script);
            final Object json = engine.get("output");
            checkState(json instanceof Map, "Expected compiled object to be an instance of Map, but it is %s",
                    Helper.getClassName(json));
            return Helper.deepCopy((Map<String, ?>)json, JSCollectionDetector.get());
        } catch (Exception e) {
            throw new ValidationException("Failed to compile DSL:\n" + dsl, e);
        }
    }

}
