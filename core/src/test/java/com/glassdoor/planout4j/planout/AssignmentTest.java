package com.glassdoor.planout4j.planout;

import com.glassdoor.planout4j.planout.ops.random.UniformChoice;
import com.glassdoor.planout4j.util.Helper;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AssignmentTest {

    private final String tester_salt = "test_salt";

    @Test
    public void testSetGetConstant() {
        Assignment a = new Assignment(tester_salt, null);
        a.set("foo", 12);
        assertEquals(12, a.get("foo"));
    }

    @Test
    public void testSetGetUniform() {
        Assignment a = new Assignment(tester_salt, null);
        Map<String, String> varNameToChoice = Map.of("foo", "b", "bar", "a", "baz", "a");
        for (String var : varNameToChoice.keySet()) {
            a.set(var, new UniformChoice<>(List.of("a", "b"), 4));
            assertEquals(varNameToChoice.get(var), a.get(var));
        }
    }

    @Test
    public void testOverrides() {
        Map<String, Object> overrides = Helper.cast(Map.of("x", 42, "y", 43));
        Assignment a = new Assignment(tester_salt, overrides);
        a.set("x", 5);
        a.set("y", 6);
        assertEquals(42, a.get("x"));
        assertEquals(43, a.get("y"));
    }

    @Test
    public void testReserved() {
        Assignment a = new Assignment(tester_salt, null);
        assertEquals(tester_salt, a.get("experiment_salt"));
        a.set("x", 42);
        assertEquals(Map.of("x", 42), a.get("data"));
    }

}