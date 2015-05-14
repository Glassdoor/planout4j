package com.glassdoor.planout4j.config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.glassdoor.planout4j.NamespaceConfig;

import static com.glassdoor.planout4j.config.KeyStrings.*;
import static com.glassdoor.planout4j.util.Helper.cast;
import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;

/**
 * Processes configuration tree (obtained from either YAML or JSON) and
 * builds {@link com.glassdoor.planout4j.NamespaceConfig} instance.
 * This class is a utility, hence there's no state and all methods are static.
 * @author ernest.mishkin
 */
public class NamespaceConfigBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(NamespaceConfigBuilder.class);

    private NamespaceConfigBuilder() {}

    /**
     * Given loaded YAML config data defining a namespace (with experiment definitions using PlanOut DSL
     * as well as sequencing) produce the corresponding JSON structure.
     * The two main goals are validation of the entire structure and compilation of PlanOut DSL to JSON.
     * @param config Map representation of a single YAML config
     * @return Map ready to be serialized into JSON
     * @throws ValidationException if any piece of data is missing or invalid; that includes DSL compilation errors
     */
    public static NamespaceConfig process(final Map<String, ?> config) throws ValidationException {
        if (config == null || config.isEmpty()) {
            throw new ValidationException("Null or empty top-level JSON object passed");
        }
        final Context ctx = new Context();

        try {
            final Map<String, Object> ns = cast(get(config, NAMESPACE, Map.class, ctx));
            ctx.push(NAMESPACE);
            final Object salt = ns.get(SALT);
            final NamespaceConfig nsConf = new NamespaceConfig(
                    get(ns, NAME, String.class, ctx),
                    get(ns, SEGMENTS, Integer.class, ctx),
                    get(ns, UNIT, String.class, ctx),
                    (salt == null ? null : salt.toString()));
            nsConf.setConfig(config);

            final List<Map<String, Object>> expDefs = cast(get(config, EXPERIMENT_DEFINITIONS, List.class, ctx));
            ctx.push(EXPERIMENT_DEFINITIONS);
            int i = 0;
            ctx.push("");
            for (Map<String, Object> expDef : expDefs) {
                ctx.replace(format("[%s]", i++));
                final Map<String, Object> assign = cast(get(expDef, ASSIGN, Map.class, ctx));
                nsConf.defineExperiment(get(expDef, DEFINITION, String.class, ctx), assign);
            }
            ctx.pop(); ctx.pop();

            nsConf.setDefaultExperiment(get(config, DEFAULT_EXPERIMENT, String.class, ctx));

            final List<Map<String, Object>> expSeq = cast(get(config, EXPERIMENT_SEQUENCE, List.class, ctx));
            ctx.push(EXPERIMENT_SEQUENCE);
            i = 0;
            ctx.push("");
            for (Map<String, Object> srcExp : expSeq) {
                ctx.replace(format("[%s]", i++));
                final String expName = get(srcExp, NAME, String.class, ctx);
                final String action = get(srcExp, ACTION, String.class, ctx);
                switch (action) {
                    case ACTION_ADD:
                        nsConf.addExperiment(expName, get(srcExp, DEFINITION, String.class, ctx),
                                get(srcExp, SEGMENTS, Integer.class, ctx));
                        break;
                    case ACTION_REMOVE:
                        nsConf.removeExperiment(expName);
                        break;
                    default:
                        throw new IllegalArgumentException(format("Unrecognized action: %s", action));
                }
            }
            ctx.pop(); ctx.pop();

            if (LOG.isTraceEnabled()) {
                LOG.trace("NS {} has {} active experiments (based on {} definitions) consuming {} segments out of {} total",
                        nsConf.name, nsConf.getActiveExperimentsCount(), nsConf.getExperimentDefsCount(),
                        nsConf.getUsedSegments(), nsConf.getTotalSegments());
            }
            nsConf.noMoreChanges();
            return nsConf;

        } catch (IllegalArgumentException e) {
            final ValidationException ve = new ValidationException(e.getMessage(), e.getCause());
            ve.setStackTrace(e.getStackTrace());
            throw ve;
        }
    }

    private static <T> T get(final Map<String, ?> map, final String key, final Class<T> valueClass, final Context context) {
        Object value = map.get(key);
        if (value instanceof String) {
            value = ((String) value).trim();
        }
        if (value instanceof Long && valueClass == Integer.class) {
            value = ((Long)value).intValue();
        }
        checkArgument(value != null, "%s key missing (%s)", key, context);
        checkArgument(valueClass.isInstance(value), "%s is expected to be an instance of %s but is an instance of %s",
                key, valueClass, value.getClass());
        try {
            checkArgument(!(Boolean)valueClass.getMethod("isEmpty").invoke(value), "%s is an empty instance of %s (%s)", key, valueClass, context);
        } catch (Exception e) {
            // assuming valueClass does not have isEmpty() method, that's OK
        }
        if (value instanceof Integer) {
            final Integer integer = cast(value);
            checkArgument(integer > 0, "%s key: all integers must be positive, this is one is %s (%s)", key, value, context);
        }
        return valueClass.cast(value);
    }


    private static class Context {
        private final Deque<String> stack = new ArrayDeque<>();

        void push(final String e) {
            stack.push(e);
        }

        String pop() {
            return stack.pop();
        }

        String replace(final String e) {
            final String old = pop();
            push(e);
            return old;
        }

        @Override
        public String toString() {
            return StringUtils.join(stack.descendingIterator(), " -> ");
        }
    }

}
