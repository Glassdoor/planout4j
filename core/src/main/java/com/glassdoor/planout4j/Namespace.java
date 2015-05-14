package com.glassdoor.planout4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.glassdoor.planout4j.planout.Interpreter;
import com.glassdoor.planout4j.util.Helper;

import static com.google.common.base.Preconditions.*;

/**
 * Namespace represents an instance of traffic segmentation for experiments management.
 * Multiple namespaces segment traffic independently.
 * A unit (such as user GUID) can be part of a single experiment within a namespace,
 * but in multiple experiments in different namespaces.<br/>
 * The experiment's parameters assignment will be eagerly evaluated and cached.
 * @author ernest.mishkin
 */
public class Namespace {

    /**
     * When present in the input map with {@link Boolean#TRUE} as the value,
     * the system will use default experiment and <b>not</b> attempt to allocate the unit to a segment.
     * <p>This is used to indicate traffic from bots, partner apps, etc.
     */
    public static final String BASELINE_KEY = "_baseline_";

    // at this point NamespaceConfig is immutable, so it's safe to expose it
    public final NamespaceConfig nsConf;

    private Experiment experiment;

    private Map<String, ?> assignments;

    /**
     * Constructs new namespace. Verifies the input is sane (has entry for the primary unit).
     * Evaluates the assignments and caches the result.
     * @param nsConf Namespace configuration
     * @param input Map of string parameter names to parameter values
     * @param overrides override parameter names/values to enable "freezing"
     *                  certain computed parameters, e.g. via query string; optional
     */
    public Namespace(final NamespaceConfig nsConf, final Map<String, ?> input, final Map<String, ?> overrides) {
        checkArgument(nsConf != null, "nsConf is required");
        checkArgument(MapUtils.isNotEmpty(input), "input map cannot be null or empty");
        this.nsConf = nsConf;
        makeAssignments(input, overrides);
    }

    /**
     * @return Cached (eagerly evaluated) Map of parameter assignments
     */
    public Map<String, ?> getParams() {
        return assignments;
    }

    /**
     * @return name of the namespace
     */
    public String getName() {
        return nsConf.name;
    }

    
   /**
    * @return The active experiment for the specified input
    */
    public Experiment getExperiment() {
      return experiment;
    }

    /**
     * Responsible for evaluating the experiment in the context of the given input and overrides.
     * @param input input context
     * @param overrides optional overrides (to freeze certain output parameters)
     * @return result of making assignments as per the experiment script.
     */
    protected Map<String, ?> evaluateExperiment(final Experiment exp, final Map<String, ?> input, final Map<String, ?> overrides) {
        final Map<String, Object> _input = Helper.cast(input), _overrides = Helper.cast(overrides);
        return Collections.unmodifiableMap(new Interpreter(exp.def.getCopyOfScript(), exp.salt, _input, _overrides).getParams());
    }

    private void makeAssignments(final Map<String, ?> input, final Map<String, ?> overrides) {
        final Experiment defaultExperiment = nsConf.getDefaultExperiment();
        // "compiler" ensures that default experiment is present, so it's safe to fail-fast here
        checkState(defaultExperiment != null, "Default experiment not set in %s namespace", nsConf.name);
        final Map<String, ?> defaultAssignments = evaluateExperiment(defaultExperiment, input, overrides);
        if (isBaseline(input)) {
            assignments = defaultAssignments;
        } else {
            experiment = nsConf.getExperiment(input);
            if (experiment != null) {
                Map<String, Object> tmp = new HashMap<>(defaultAssignments); // the tmp trick is due to generics issues
                final Map<String, ?> specificAssignments = evaluateExperiment(experiment, input, overrides);
                tmp.putAll(specificAssignments);
                assignments = tmp;
            } else {
                assignments = defaultAssignments;
            }
        }
    }

    protected boolean isBaseline(final Map<String, ?> input) {
        final Object baseline = input.get(BASELINE_KEY);
        return baseline instanceof Boolean && (Boolean)baseline;
    }


    public int getParam(final String key, final int def) {
        return getParams().containsKey(key) ? ((Number)getParams().get(key)).intValue() : def;
    }

    public float getParam(final String key, final float def) {
        return getParams().containsKey(key) ? ((Number)getParams().get(key)).floatValue() : def;
    }

    public boolean getParam(final String key, final boolean def) {
        return getParams().containsKey(key) ? ((Boolean)getParams().get(key)) : def;
    }

    public String getParam(final String key, final String def) {
        return getParams().containsKey(key) ? ((String)getParams().get(key)) : def;
    }

}
