package com.glassdoor.planout4j.planout.ops.core;

import java.util.List;
import java.util.Map;

import com.glassdoor.planout4j.planout.ops.base.PlanOutOpSimple;
import com.glassdoor.planout4j.planout.ops.utils.Operators;

import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;

/**
 * Getting an item from a list or a map.
 * Returns value at index if it exists, returns null otherwise.
 */
public class Index extends PlanOutOpSimple<Object> {

    public Index(final Map<String, Object> args) {
        super(args);
    }

    @Override
    protected Object simpleExecute() {
        final Object base = getArgIndexish("base");
        final Object index = getArgMixed("index");
        if (base instanceof List) {
            final List list = (List)base;
            checkState(index instanceof Integer,
                    "%s: index must be an int when accessing a list, but it is %s", getClass(), index.getClass());
            final int i = (Integer)index;
            return i >= 0 && i < list.size() ? list.get(i) : null;
        } else {
            return ((Map)base).get(index);
        }
    }

    @Override
    public String pretty() {
        return format("%s[%s]", Operators.pretty(getArgMixed("base")), Operators.pretty(getArgMixed("index")));
    }
    
}
