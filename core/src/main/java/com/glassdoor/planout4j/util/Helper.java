package com.glassdoor.planout4j.util;

import java.util.*;

import org.apache.commons.collections4.ComparatorUtils;

import com.google.common.base.MoreObjects;

import com.glassdoor.planout4j.planout.ops.utils.MixedNumbersComparator;

import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;

/**
 * Helper methods not explicitly corresponding to the original python code.
 * @author ernest.mishkin
 */
public class Helper {

    /**
     * Utility method to perform generic casts without dealing with <i>Unchecked cast</i> warnings
     * (or having to apply <code>SuppressWarnings("unchecked")</code> annotations).
     * @param o Object which is presumably of type T
     * @param <T> type to cast to
     * @return the object as instance of T
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object o) {
        return (T)o;
    }

    /**
     * Attempts to interpret a given object as boolean.
     * Behaves consistently with python's rules.
     * @param o Object, possibly null
     * @return boolean
     * @throws java.lang.IllegalStateException if an object is of unrecognized type
     */
    public static boolean asBoolean(final Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Number) {
            return ((Number)o).doubleValue() != 0;
        }
        if (o instanceof Boolean) {
            return (Boolean)o;
        }
        // the below code covers String, List, Map, etc.
        try {
            return !((Boolean)o.getClass().getMethod("isEmpty").invoke(o));
        } catch (Exception e) {
            throw new IllegalStateException(format("Don't know how to evaluate an instance of %s as boolean", o.getClass()), e);
        }
    }

    /**
     * Null-safe way of getting object's class name.
     * @param o Object
     * @return object's class name or string "null"
     */
    public static String getClassName(final Object o) {
        return o == null ? "null" : o.getClass().getName();
    }

    /**
     * "Boxes" a double value as either Double or Long depending on the integer flag.
     * @param n the number to wrap
     * @return Number the appropriate wrapper instance
     */
    public static Number wrapNumber(final double n, final boolean integer) {
        if (integer) {
            return (long)n;
        } else {
            return n;
        }
    }

    /**
     * Whether the given wrapper instance denotes a real number.
     * @param n number
     * @return true, if real; false, if integer
     */
    public static boolean isRealNumber(final Number n) {
        return n instanceof Double || n instanceof Float;
    }

    /**
     * Given a list of objects, determines the right comparator to use.
     * If all elements are numbers, use special comparator which treats all numeric wrapper types the same.
     * Otherwise use default "natural" comparator.
     * Validates that all elements are comparable as well as numbers and non-numbers aren't mixed together.
     * @param values List of values
     * @param caller the calling Class (for better context in error reporting)
     * @return either {@link com.glassdoor.planout4j.planout.ops.utils.MixedNumbersComparator} or
     *         {@link org.apache.commons.collections4.ComparatorUtils#NATURAL_COMPARATOR}
     */
    public static Comparator getComparator(final List<Object> values, final Class caller) {
        boolean useMixedNumbersComparator = false;
        for (Object value : values) {
            if (value instanceof Number) {
                useMixedNumbersComparator = true;
            } else {
                checkState(value == null || value instanceof Comparable,
                        "%s: non-comparable object of type %s", caller, getClassName(value));
                checkState(!useMixedNumbersComparator,
                        "%s: mixed numbers / not numbers array, can't compare", caller);
            }
        }
        return useMixedNumbersComparator ? MixedNumbersComparator.INSTANCE : ComparatorUtils.NATURAL_COMPARATOR;
    }


    /**
     * Performs deep (recursive) copy of PlanOut script parse tree (represented by combination of maps and lists).
     * @param script the "tree" to copy
     * @return Map representing the newly created copy
     */
    public static Map<String, ?> deepCopy(final Map<String, ?> script, final CollectionDetector collectionDetector) {
        // the copy can possibly grow by 1 entry due to "salt" key being added,
        // hence the small optimization with capacity and load factor
        final Map<String, Object> copy = new LinkedHashMap<>(script.size()+1, 1);
        final CollectionDetector notNullCollectionDetector = MoreObjects.firstNonNull(collectionDetector, CollectionDetector.DEFAULT);
        for (String key : script.keySet()) {
            copy.put(key, copyIfNecessary(script.get(key), notNullCollectionDetector));
        }
        return copy;
    }

    private static List<?> deepCopy(final Collection<?> list, final CollectionDetector collectionDetector) {
        final List<Object> copy = new ArrayList<>(list.size());
        for (Object val : list) {
            copy.add(copyIfNecessary(val, collectionDetector));
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static Object copyIfNecessary(final Object val,final CollectionDetector collectionDetector) {
        if (collectionDetector.isCollection(val)) {
            return deepCopy(collectionDetector.extractCollection(val), collectionDetector);
        } else if (val instanceof Map) {
            return deepCopy((Map<String, ?>)val, collectionDetector);
        } else {
            return val;
        }
    }


    private Helper() {}
}
