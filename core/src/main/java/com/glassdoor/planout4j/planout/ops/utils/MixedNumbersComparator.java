package com.glassdoor.planout4j.planout.ops.utils;

import java.util.Comparator;

/**
 * Comparator which can can compare different {@link java.lang.Number} subclasses
 * (e.g. {@link java.lang.Integer} and {@link java.lang.Double}).
 * While this breaks the formal contract of {@link java.lang.Comparable#compareTo(Object)} it is necessary
 * because mixing integer and real numbers is entirely normal.
 */
public class MixedNumbersComparator implements Comparator<Number> {

    /** This class has no state, why not to use it as singleton */
    public static final MixedNumbersComparator INSTANCE = new MixedNumbersComparator();

    @Override
    public int compare(final Number o1, final Number o2) {
        return Double.compare(o1.doubleValue(), o2.doubleValue());
    }

}
