package com.glassdoor.planout4j.planout.ops.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.planout.ops.core.*;
import com.glassdoor.planout4j.planout.ops.random.*;
import com.glassdoor.planout4j.util.Helper;

import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;

/**
 * All supported operators mapped to the {@link com.glassdoor.planout4j.planout.ops.base.PlanOutOp} descendant classes.
 * Also various util methods.
 */
public class Operators {

    /** maps operator string representation to implementing class */
    public static final Map<String, Class<? extends PlanOutOp>> operators;
    static {
        operators = new ImmutableMap.Builder<String, Class<? extends PlanOutOp>>()
                .put("literal", Literal.class)
                .put("get", Get.class)
                .put("seq", Seq.class)
                .put("set", Set.class)
                .put("return", Return.class)
                .put("index", Index.class)
                .put("array", Array.class)
                .put("equals", Equals.class)
                .put("cond", Cond.class)
                .put("and", And.class)
                .put("or", Or.class)
                .put(">", GreaterThan.class)
                .put("<", LessThan.class)
                .put(">=", GreaterThanOrEqualTo.class)
                .put("<=", LessThanOrEqualTo.class)
                .put("%", Mod.class)
                .put("/", Divide.class)
                .put("not", Not.class)
                .put("round", Round.class)
                .put("negative", Negative.class)
                .put("min", Min.class)
                .put("max", Max.class)
                .put("length", Length.class)
                .put("coalesce", Coalesce.class)
                .put("product", Product.class)
                .put("sum", Sum.class)
                .put("randomFloat", RandomFloat.class)
                .put("randomInteger", RandomInteger.class)
                .put("bernoulliTrial", BernoulliTrial.class)
                .put("bernoulliFilter", BernoulliFilter.class)
                .put("uniformChoice", UniformChoice.class)
                .put("weightedChoice", WeightedChoice.class)
                .put("sample", Sample.class)
                .build();
    }

    public static boolean isOperator(final Object op) {
        return op instanceof Map && ((Map)op).containsKey("op");
    }

    public static PlanOutOp operatorInstance(final Object params) {
        final String op = op(params);
        final Class<? extends PlanOutOp> implClass = operators.get(op);
        checkState(implClass != null, "Unknown operator: %s", op);
        try {
            return implClass.getConstructor(Map.class).newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException(format("Failed to instantiate %s", implClass), e);
        }
    }

    public static String prettyParamFormat(final Map<String, Object> params) {
        final List<String> ps = new ArrayList<>();
        for (Entry<String, Object> entry : params.entrySet()) {
            if (!"op".equals(entry.getKey())) {
                ps.add(format("%s=%s", entry.getKey(), pretty(entry.getValue())));
            }
        }
        return StringUtils.join(ps, ", ");
    }

    public static String pretty(final Object params) {
        if (isOperator(params)) {
            try {
                return operatorInstance(params).pretty();
            } catch (Exception e) {
                return params.toString();
            }
        } else if (params instanceof List) {
            return format("[%s]", join((List)params));
        } else {
            return params.toString();
        }
    }

    public static Object stripArray(final Object params) {
        if (params instanceof List) {
            return params;
        } else if (isOperator(params) && "array".equals(op(params))) {
            return get(params, "values");
        } else {
            return params;
        }
    }

    private static String op(final Object params) {
        checkState(isOperator(params));
        return String.valueOf(get(params, "op"));
    }

    public static Object get(final Object params, final String name) {
        return ((Map)params).get(name);
    }

    public static void set(final Object params, final String name, final Object value) {
        final Map<String, Object> map = Helper.cast(params);
        map.put(name, value);
    }

    /**
     * Pretty-format all items of the list and combine into single string.
     * @param params List of items
     * @param sep separator string
     * @return String joined pretty representations of all params
     */
    public static String join(final List params, final String sep) {
        final List<String> list = new ArrayList<>();
        for (Object value : params) {
            list.add(pretty(value));
        }
        return StringUtils.join(list, sep);
    }

    /**
     * Pretty-format all items of the list and combine into single string.
     * @param params List of items
     * @return String joined pretty representations of all params
     */
    public static String join(final List params) {
        return join(params, ", ");
    }

    public static String indent(final String s, final int n) {
        final List<String> l = new ArrayList<>();
        for (String i : s.split("\n")) {
            l.add(Strings.repeat("  ", n) + i);
        }
        return StringUtils.join(l, '\n');
    }

    private Operators() {}

}
