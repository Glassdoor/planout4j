package com.glassdoor.planout4j.planout.ops;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.glassdoor.planout4j.planout.Interpreter;
import com.glassdoor.planout4j.planout.ops.utils.MixedNumbersComparator;

import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
public class CoreOpsTest {

    private Interpreter runConfigRaw(Object config) {
        return new Interpreter(config, "test_salt", null, null);
    }

    private Map<String, Object> runConfig(Object config) {
        final Interpreter e = runConfigRaw(config);
        return e.getParams();
    }

    private Object runConfigSingle(Object config) {
        Object xConfig = new JSONObjectBuilder().p("op", "set").p("value", config).p("var", "x");
        return runConfig(xConfig).get("x");
    }

    @Test
    public void testSet() {
        Object c = new JSONObjectBuilder().p("op", "set").p("value", "x_val").p("var", "x");
        Map<String, Object> d = runConfig(c);
        assertEquals(ImmutableMap.of("x", "x_val"), d);
    }

    @Test
    public void testSeq() {
        Object c = new JSONObjectBuilder().p("op", "seq").p("seq", new JSONArrayBuilder()
                        .a(new JSONObjectBuilder().p("op", "set").p("value", "x_val").p("var", "x"))
                        .a(new JSONObjectBuilder().p("op", "set").p("value", "y_val").p("var", "y"))
        );
        Map <String, Object> d = runConfig(c);
        assertEquals(ImmutableMap.of("x", "x_val", "y", "y_val"), d);
    }

    @Test
    public void testEq() {
        Object c1 = new JSONObjectBuilder().p("op", "seq").p("seq", new JSONArrayBuilder()
                        .a(new JSONObjectBuilder().p("op", "set").p("value", "x_val").p("var", "x"))
                        .a(new JSONObjectBuilder().p("op", "set").p("value", "y_val").p("var", "y"))
        );
        Object c2 = new JSONObjectBuilder().p("op", "seq").p("seq", new JSONArrayBuilder()
                        .a(new JSONObjectBuilder().p("op", "set").p("value", "x_val").p("var", "x"))
                        .a(new JSONObjectBuilder().p("op", "set").p("value", "y_val").p("var", "y"))
        );
        assertTrue(c1.equals(c2));
        assertTrue(c1.hashCode() == c2.hashCode());
        Object c3 = new JSONObjectBuilder().p("op", "seq").p("seq", new JSONArrayBuilder()
                        .a(new JSONObjectBuilder().p("op", "set").p("value", "x_val").p("var", "x"))
                        .a(new JSONObjectBuilder().p("op", "set").p("value", "z_val").p("var", "y"))
        );
        assertFalse(c1.equals(c3));
    }

    @Test
    public void testArray() {
        Object arr = new JSONArrayBuilder().a(4).a(5).a("a");
        Object a = runConfigSingle(new JSONObjectBuilder().p("op", "array").p("values", arr));
        assertEquals(arr, a);
        arr = new JSONArray();
        a = runConfigSingle(new JSONObjectBuilder().p("op", "array").p("values", arr));
        assertEquals(arr, a);
    }

    @Test
    public void testMap() {
        Map map = new JSONObjectBuilder().p("a", 1).p("b", "1").p("c", true).p("d", new JSONArrayBuilder().a(1).a(2));
        Object m = runConfigSingle(new JSONObjectBuilder(map).p("op", "map"));
        assertEquals(map, m);
        map = new JSONObject();
        m = runConfigSingle(new JSONObjectBuilder(map).p("op", "map"));
        assertEquals(map, m);
    }

    @Test
    public void testCond() {
        for (int i=0; i < 2; i++) {
            Object c = new JSONObjectBuilder().p("op", "cond").p("cond", new JSONArrayBuilder()
                            .a(new JSONObjectBuilder()
                                    .p("if", new JSONObjectBuilder().p("op", "equals").p("left", i).p("right", 0))
                                    .p("then", new JSONObjectBuilder().p("op", "set").p("value", "x_0").p("var", "x")))
                            .a(new JSONObjectBuilder()
                                    .p("if", new JSONObjectBuilder().p("op", "equals").p("left", i).p("right", 1))
                                    .p("then", new JSONObjectBuilder().p("op", "set").p("value", "x_1").p("var", "x")))
            );
            assertEquals(ImmutableMap.of("x", "x_"+i), runConfig(c));
        }
        Object c = new JSONObjectBuilder().p("op", "cond").p("cond", new JSONArrayBuilder()
                .a(new JSONObjectBuilder()
                        .p("if", new JSONObjectBuilder().p("op", "equals").p("left", 1).p("right", 0))
                        .p("then", new JSONObjectBuilder().p("op", "set").p("value", "x_0").p("var", "x")))
        );
        assertEquals(ImmutableMap.of(), runConfig(c));
    }

    @Test
    public void testGet() {
        Object d = runConfig(
                new JSONObjectBuilder().p("op", "seq").p("seq", new JSONArrayBuilder()
                                .a(new JSONObjectBuilder().p("op", "set").p("value", "x_val").p("var", "x"))
                                .a(new JSONObjectBuilder().p("op", "set").p("value", new JSONObjectBuilder().p("op", "get").p("var", "x")).p("var", "y"))
                )
        );
        assertEquals(ImmutableMap.of("x", "x_val", "y", "x_val"), d);
    }

    @Test
    public void testIndex() {
        Object arrayLiteral = new JSONArrayBuilder().a(10).a(20).a(30);
        Object dictLiteral = new JSONObjectBuilder().p("a", 42).p("b", 43);
        // basic indexing works with array literals
        Object x = runConfigSingle(new JSONObjectBuilder().p("op", "index").p("index", 0).p("base", arrayLiteral));
        assertEquals(10, x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "index").p("index", 2).p("base", arrayLiteral));
        assertEquals(30, x);
        // basic indexing works with dictionary literals
        x = runConfigSingle(new JSONObjectBuilder().p("op", "index").p("index", "a").p("base", dictLiteral));
        assertEquals(42, x);
        // invalid indexes are mapped to null
        x = runConfigSingle(new JSONObjectBuilder().p("op", "index").p("index", 6).p("base", arrayLiteral));
        assertNull(x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "index").p("index", "c").p("base", dictLiteral));
        assertNull(x);
        // non literals also work
        x = runConfigSingle(new JSONObjectBuilder().p("op", "index").p("index", 2)
                .p("base", new JSONObjectBuilder().p("op", "array").p("values", arrayLiteral)));
        assertEquals(30, x);
    }

    @Test
    public void testCoalesce() {
        Object x = runConfigSingle(new JSONObjectBuilder().p("op", "coalesce").p("values", Collections.singletonList(null)));
        assertNull(x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "coalesce").p("values", Arrays.asList(null, 42, null)));
        assertEquals(42, x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "coalesce").p("values", Arrays.asList(null, null, 43)));
        assertEquals(43, x);
    }

    @Test
    public void testLength() {
        // arrays, literal and not
        List array = ImmutableList.of(0, 1, 2, 3, 4);
        Object lengthTest = runConfigSingle(new JSONObjectBuilder().p("op", "length").p("value", array));
        assertEquals(array.size(), lengthTest);
        lengthTest = runConfigSingle(new JSONObjectBuilder().p("op", "length").p("value", Collections.EMPTY_LIST));
        assertEquals(0, lengthTest);
        lengthTest = runConfigSingle(new JSONObjectBuilder().p("op", "length").p("value", new JSONObjectBuilder().p("op", "array").p("values", array)));
        assertEquals(array.size(), lengthTest);
        // dict
        Map dict = ImmutableMap.of("a", 1, "b", 2);
        lengthTest = runConfigSingle(new JSONObjectBuilder().p("op", "length").p("value", dict));
        assertEquals(dict.size(), lengthTest);
        lengthTest = runConfigSingle(new JSONObjectBuilder().p("op", "length").p("value", Collections.EMPTY_MAP));
        assertEquals(0, lengthTest);
        // string
        String string = "abc";
        lengthTest = runConfigSingle(new JSONObjectBuilder().p("op", "length").p("value", string));
        assertEquals(string.length(), lengthTest);
    }

    @Test
    public void testNot() {
        // equivalents of false
        Object x = runConfigSingle(new JSONObjectBuilder().p("op", "not").p("value", 0));
        assertTrue((Boolean)x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "not").p("value", false));
        assertTrue((Boolean) x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "not").p("value", Collections.EMPTY_LIST));
        assertTrue((Boolean) x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "not").p("value", ""));
        assertTrue((Boolean)x);
        // equivalents of true
        x = runConfigSingle(new JSONObjectBuilder().p("op", "not").p("value", 1));
        assertFalse((Boolean) x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "not").p("value", true));
        assertFalse((Boolean) x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "not").p("value", ImmutableMap.of("a", 42)));
        assertFalse((Boolean) x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "not").p("value", "abc"));
        assertFalse((Boolean) x);
    }

    @Test
    public void testOr() {
        Object x = runConfigSingle(new JSONObjectBuilder().p("op", "or").p("values", ImmutableList.of(0, 0, 0)));
        assertFalse((Boolean)x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "or").p("values", ImmutableList.of(0, 0, 1)));
        assertTrue((Boolean) x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "or").p("values", ImmutableList.of(false, true, false)));
        assertTrue((Boolean) x);
    }

    @Test
    public void testAnd() {
        Object x = runConfigSingle(new JSONObjectBuilder().p("op", "and").p("values", ImmutableList.of(1, 1, 0)));
        assertFalse((Boolean)x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "and").p("values", ImmutableList.of(0, 0, 1)));
        assertFalse((Boolean) x);
        x = runConfigSingle(new JSONObjectBuilder().p("op", "and").p("values", ImmutableList.of(true, true, true)));
        assertTrue((Boolean) x);
    }

    @Test
    public void testCommutative() {
        List arr = ImmutableList.of(33, 7, 18, 21, -3, 10000, 10000);
        List arrReal = ImmutableList.of(33, 7.0, 18, 21, -3, 10000, 10000);
        Object minTest = runConfigSingle(new JSONObjectBuilder().p("op", "min").p("values", arr));
        assertEquals(Collections.min(arr), minTest);
        minTest = runConfigSingle(new JSONObjectBuilder().p("op", "min").p("values", arrReal));
        assertEquals(Collections.min(arrReal, MixedNumbersComparator.INSTANCE), minTest);
        Object maxTest = runConfigSingle(new JSONObjectBuilder().p("op", "max").p("values", arr));
        assertEquals(Collections.max(arr), maxTest);
        Object sumTest = runConfigSingle(new JSONObjectBuilder().p("op", "sum").p("values", arr));
        assertEquals(20076l, sumTest);
        Object sumTestReal = runConfigSingle(new JSONObjectBuilder().p("op", "sum").p("values", arrReal));
        assertEquals(20076.0, sumTestReal);
        Object productTest = runConfigSingle(new JSONObjectBuilder().p("op", "product").p("values", arr));
        assertEquals(-26195400000000l, productTest);
        Object productTestReal = runConfigSingle(new JSONObjectBuilder().p("op", "product").p("values", arrReal));
        assertEquals(-26195400000000.0, productTestReal);
    }

    @Test
    public void testBinaryOps() {
        Object eq = runConfigSingle(new JSONObjectBuilder().p("op", "equals").p("left", 1).p("right", 2));
        assertEquals(1 == 2, eq);
        eq = runConfigSingle(new JSONObjectBuilder().p("op", "equals").p("left", 2).p("right", 2l));
        assertEquals(2 == 2l, eq);
        Object gt = runConfigSingle(new JSONObjectBuilder().p("op", ">").p("left", 1).p("right", 2));
        assertEquals(1 > 2, gt);
        Object lt = runConfigSingle(new JSONObjectBuilder().p("op", "<").p("left", 1).p("right", 2.0));
        assertEquals(1.0 < 2.0, lt);
        Object gte = runConfigSingle(new JSONObjectBuilder().p("op", ">=").p("left", 2).p("right", 2.0));
        assertEquals(2.0 >= 2.0, gte);
        gte = runConfigSingle(new JSONObjectBuilder().p("op", ">=").p("left", 1).p("right", 2));
        assertEquals(1 >= 2, gte);
        Object lte = runConfigSingle(new JSONObjectBuilder().p("op", "<=").p("left", 2.0).p("right", 2.0));
        assertEquals(2.0 <= 2.0, lte);
        Object mod = runConfigSingle(new JSONObjectBuilder().p("op", "%").p("left", 11).p("right", 3));
        assertEquals((long)(11 % 3), mod);
        Object div = runConfigSingle(new JSONObjectBuilder().p("op", "/").p("left", 3).p("right", 4));
        assertEquals(0.75, div);
    }

    @Test
    public void testUnaryOps() {
        Object negative = runConfigSingle(new JSONObjectBuilder().p("op", "negative").p("value", 42.0));
        assertEquals(-42.0, negative);
        negative = runConfigSingle(new JSONObjectBuilder().p("op", "negative").p("value", 42));
        assertEquals(-42l, negative);
        Object round = runConfigSingle(new JSONObjectBuilder().p("op", "round").p("value", 42));
        assertEquals(42l, round);
        round = runConfigSingle(new JSONObjectBuilder().p("op", "round").p("value", 0.75));
        assertEquals(1l, round);
    }

    @Test
    public void testExp() {
        Object exp =  runConfigSingle(new JSONObjectBuilder().p("op", "exp").p("value", 1));
        assertEquals(2.718281828459045, exp);
        exp = runConfigSingle(new JSONObjectBuilder().p("op", "exp").p("value", -0.5));
        assertEquals(0.6065306597126334, exp);
        exp = runConfigSingle(new JSONObjectBuilder().p("op", "exp").p("value", 0.123));
        assertEquals(1.1308844209474893, exp);
        exp = runConfigSingle(new JSONObjectBuilder().p("op", "exp").p("value", 1.88));
        assertEquals(6.553504862191148, exp);
    }

    @Test
    public void testSqrt() {
        Object sqrt =  runConfigSingle(new JSONObjectBuilder().p("op", "sqrt").p("value", 1));
        assertEquals(1.0, sqrt);
        sqrt = runConfigSingle(new JSONObjectBuilder().p("op", "sqrt").p("value", 0.123));
        assertEquals(0.3507135583350036, sqrt);
        sqrt = runConfigSingle(new JSONObjectBuilder().p("op", "sqrt").p("value", 1.88));
        assertEquals(1.3711309200802089, sqrt);
        try {
            sqrt = runConfigSingle(new JSONObjectBuilder().p("op", "sqrt").p("value", -0.5));
        } catch (IllegalArgumentException e) {}
    }

    private Interpreter returnRunner(Object value) {
        Object c = new JSONObjectBuilder().p("op", "seq").p("seq", new JSONArrayBuilder()
                        .a(new JSONObjectBuilder().p("op", "set").p("value", 2).p("var", "x"))
                        .a(new JSONObjectBuilder().p("op", "return").p("value", value))
                        .a(new JSONObjectBuilder().p("op", "set").p("value", 4).p("var", "y"))
        );
        return runConfigRaw(c);
    }

    @Test
    public void testReturn() {
        Interpreter i = returnRunner(true);
        assertEquals(ImmutableMap.of("x", 2), i.getParams());
        assertTrue(i.isInExperiment());
        i = returnRunner(42);
        assertEquals(ImmutableMap.of("x", 2), i.getParams());
        assertTrue(i.isInExperiment());
        i = returnRunner(false);
        assertEquals(ImmutableMap.of("x", 2), i.getParams());
        assertFalse(i.isInExperiment());
        i = returnRunner(0);
        assertEquals(ImmutableMap.of("x", 2), i.getParams());
        assertFalse(i.isInExperiment());
    }

}
