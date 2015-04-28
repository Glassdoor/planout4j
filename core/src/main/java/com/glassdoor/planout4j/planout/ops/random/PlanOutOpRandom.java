package com.glassdoor.planout4j.planout.ops.random;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.google.common.hash.Hashing;

import com.glassdoor.planout4j.planout.Assignment;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOpSimple;
import com.glassdoor.planout4j.util.Helper;

import static java.lang.String.format;

/**
 * Abstract base class for all random operators.
 */
public abstract class PlanOutOpRandom<T> extends PlanOutOpSimple<T> {

    private static final Charset UTF8 = Charset.forName("utf-8");

    public static final double LONG_SCALE = 0xFFFFFFFFFFFFFFFl;

    protected PlanOutOpRandom(final Map<String, Object> args) {
        super(args);
    }

    protected PlanOutOpRandom(final Object unit) {
        if (unit != null) {
            args.put("unit", unit);
        }
    }

    protected List getUnit(final Object appendedUnit) {
        final Object o = getArgMixed("unit");
        List<Object> unit;
        if (o instanceof List) {
            unit = Helper.cast(o);
        } else {
            unit = new ArrayList<>();
            unit.add(o);
        }
        if (appendedUnit != null) {
            unit.add(appendedUnit);
        }
        return unit;
    }

    protected List getUnit() {
        return getUnit(null);
    }

    /**
     * Generate a pseudo-random value, deterministic relative to appendUnit and salts
     * @param appendUnit    unit of randomization
     * @return              long value in the range [0 .. LONG_SCALE]
     */
    protected long getHash(final Object appendUnit) {
        String fullSalt;
        if (args.containsKey("full_salt")) {
            fullSalt = getArgString("full_salt");
        } else {
            final String salt = getArgString("salt");
            fullSalt = format("%s.%s", mapper.getExperimentSalt(), salt);
        }
        final String unitStr = StringUtils.join(getUnit(appendUnit), '.');
        final String hashStr = format("%s.%s", fullSalt, unitStr);
        return Long.parseLong(Hashing.sha1().hashString(hashStr, UTF8).toString().substring(0,15), 16);
    }

    protected long getHash() {
        return getHash(null);
    }

    /**
     * Generate pseudo-random value with uniform distribution, deterministic relative to appendUnit and salts
     * @param minVal
     * @param maxVal
     * @param appendUnit
     * @return      double value in the range [minVal .. maxVal]
     */
    protected double getUniform(final double minVal, final double maxVal, final Object appendUnit) {
        final double zeroToOne = getHash(appendUnit) / LONG_SCALE;
        return minVal + (maxVal - minVal) * zeroToOne;
    }

    /**
     * Generate pseudo-random value with uniform distribution, deterministic relative to salts
     * @param minVal
     * @param maxVal
     * @return      double value in the range [minVal .. maxVal]
     */
    protected double getUniform(final double minVal, final double maxVal) {
        return getUniform(minVal, maxVal, null);
    }

    /**
     * Generate pseudo-random value with uniform distribution, deterministic relative to salts
     * @return      double value in the range [0.0 .. 1.0]
     */
    protected double getUniform() {
        return getUniform(0.0, 1.0, null);
    }

    /**
     * For public use outside of the interpreter flow.
     * @return result of executing the operation assuming all its arguments are already evaluated
     */
    public T eval() {
        final Assignment assignment = new Assignment(null, null);
        assignment.set("x", this);
        return Helper.cast(assignment.get("x"));
    }

}
