package com.glassdoor.planout4j.planout.ops.core;

import java.util.Map;

import com.glassdoor.planout4j.planout.Mapper;
import com.glassdoor.planout4j.planout.ops.base.PlanOutOp;
import com.glassdoor.planout4j.planout.ops.utils.Operators;
import com.glassdoor.planout4j.util.Helper;

import static java.lang.String.format;

/**
 * Conditional (if ... else if ... else) statement.
 */
public class Cond extends PlanOutOp<Object> {

    public Cond(final Map<String, Object> args) {
        super(args);
    }

    @Override
    public Object execute(final Mapper mapper) {
        for (Object i : getArgList("cond")) {
            final Map<String, Object> cond = Helper.cast(i);
            if (Helper.asBoolean(mapper.evaluate(cond.get("if")))) {
                return mapper.evaluate(cond.get("then"));
            }
        }
        return null;
    }

    @Override
    public String pretty() {
        final StringBuilder prettyStr = new StringBuilder();
        boolean firstIf = true;
        for (Object i : getArgList("cond")) {
            final Map<String, Object> cond = Helper.cast(i);
            final Object ifClause = cond.get("if");
            if ("true".equals(ifClause)) {
                prettyStr.append("else");
            } else {
                final String prefix = firstIf ? "if(%s)" : "else if(%s)";
                prettyStr.append(format(prefix, Operators.pretty(ifClause)));
                firstIf = false;
            }
            prettyStr.append(" {\n").append(Operators.indent(Operators.pretty(cond.get("then")), 1)).append("\n}");
        }
        return prettyStr.toString();
    }

}
