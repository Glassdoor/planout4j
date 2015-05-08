package com.glassdoor.planout4j;


import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.glassdoor.planout4j.planout.ops.random.RandomInteger;
import com.glassdoor.planout4j.planout.ops.random.Sample;

import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;

/**
 * NamespaceConfig maintains the "static" state of namespace (data that doesn't change at runtime) and includes<ul>
 *     <li>name, primary unit, optional salt</li>
 *     <li>all experiment definitions keyed by the definition string</li>
 *     <li>default experiment (required)</li>
 *     <li>all active experiments keyed by name</li>
 *     <li>collection of available segments</li>
 *     <li>segments allocation map (which experiment a segment is assigned to)</li>
 * </ul>
 * @see com.glassdoor.planout4j.config.NamespaceConfigBuilder
 * @author ernest.mishkin
 */
public class NamespaceConfig {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public final String name;
    public final String unit;
    public final String salt;

    private final Map<String, ExperimentConfig> allExpDefs = new LinkedHashMap<>();
    private final Map<String, Experiment> activeExperiments = new LinkedHashMap<>();

    private final Set<Integer> availableSegments;
    private final Experiment[] allocationMap;

    private Experiment defaultExperiment;


    /**
     * Constructs new namespace config with the namespace-level data.
     * After construction, it is expected that [define/add/remove]Experiment methods will be called a number of times
     * to complete the configuration. Once complete, this object should not be further modified.
     * @param name namespace name
     * @param totalSegments total number of segments to split the traffic into, must be positive int
     * @param unit name of the primary unit (key in the input parameters)
     * @param salt optional salt, if not specified the name is used
     */
    public NamespaceConfig(final String name, final int totalSegments, final String unit, final String salt) {
        checkArgument(StringUtils.isNotEmpty(name));
        this.name = name;
        checkArgument(StringUtils.isNotEmpty(unit));
        this.unit = unit;
        checkArgument(totalSegments > 0, "totalSegments must be a positive integer");
        availableSegments = new HashSet<>(totalSegments, 1);
        for (int i=0; i < totalSegments; i++) {
            availableSegments.add(i);
        }
        allocationMap = new Experiment[totalSegments];
        this.salt = StringUtils.stripToNull(salt);
    }

    /**
     * Defines a new experiment. Simply associates a key with an assign script.
     * Does not validate the script. The key must be unique of course.
     * @param definition the key to access the script
     * @param assign the assignment script (e.g. compiled PlanOut DSL or Darwin JSON)
     */
    public void defineExperiment(final String definition, final Map<String, ?> assign) {
        final ExperimentConfig expDef = new ExperimentConfig(definition, assign);
        final ExperimentConfig existingDef = allExpDefs.put(definition, expDef);
        checkArgument(existingDef == null, "duplicated experiment definition %s", expDef);
    }

    /**
     * Instantiates an experiment with a given name and number of segments.
     * @param expName experiment name
     * @param definition definition key used in {@link #defineExperiment(String, java.util.Map)}
     * @param segments number of segments to allocate, must be no more than remaining available
     */
    public void addExperiment(final String expName, final String definition, final int segments) {
        final ExperimentConfig expDef = allExpDefs.get(definition);
        checkArgument(expDef != null, "reference to undefined experiment %s", definition);
        final Collection<Integer> usedSegments = allocateSegments(segments, expName);
        final String expSalt = format("%s.%s", StringUtils.defaultString(salt, this.name), expName);
        final Experiment exp = new Experiment(expName, expSalt, expDef, usedSegments);
        final Experiment existingExp = activeExperiments.put(expName, exp);
        checkArgument(existingExp == null, "duplicate experiment name %s", expName);
        for (Integer segment : usedSegments) {
            allocationMap[segment] = exp;
        }
    }

    /**
     * Removes an experiment and releases its segments into the available pool.
     * @param expName experiment name
     */
    public void removeExperiment(final String expName) {
        final Experiment exp = activeExperiments.remove(expName);
        checkArgument(exp != null, "No active experiment named %s", expName);
        availableSegments.addAll(exp.usedSegments);
        for (Integer segment : exp.usedSegments) {
            // sanity check
            checkState(allocationMap[segment] == exp,
                    "Segment %s is supposed to be allocated to experiment %s but is allocated to %s instead",
                    segment, exp, allocationMap[segment]);
            allocationMap[segment] = null;
        }
    }

    /**
     * Set default experiment. The experiment name is same as definition key.
     * @param definition the definition key
     */
    public void setDefaultExperiment(final String definition) {
        final ExperimentConfig expDef = allExpDefs.get(definition);
        checkArgument(expDef != null, "reference to undefined experiment %s", definition);
        final String expSalt = format("%s.%s", StringUtils.defaultString(salt, this.name), definition);
        defaultExperiment = new Experiment(definition, expSalt, expDef, null);
    }

    /**
     * @return number of defined experiments
     */
    public int getExperimentDefsCount() {
        return allExpDefs.size();
    }

    /**
     * Get an active experiment config by its definition key (primarily for debugging purposes).
     * @param definition definition key
     * @return ExperimentConfig (null if none with the specified definition as key)
     */
    public ExperimentConfig getExperimentConfig(final String definition) {
        return allExpDefs.get(definition);
    }

    /**
     * @return number of active experiment instances
     */
    public int getActiveExperimentsCount() {
        return activeExperiments.size();
    }

    /**
     * Get an active experiment by name (primarily for debugging purposes).
     * @param name experiment name
     * @return Experiment instance, null if there is no active experiment with the specified name
     */
    public Experiment getActiveExperiment(final String name) {
        return activeExperiments.get(name);
    }

    /**
     * @return total number of segments
     */
    public int getTotalSegments() {
        return allocationMap.length;
    }

    /**
     * @return number of segments used
     */
    public int getUsedSegments() {
        return getTotalSegments() - availableSegments.size();
    }

    /**
     * Maps the primary unit to segment and segment to experiment.
     * This is the main API for {@link Namespace} class.
     * @param input input context, must at least contain the entry for the primary unit
     * @return Experiment allocated to the corresponding segment, may be null
     */
    public Experiment getExperiment(final Map<String, ?> input) {
        return getExperiment(getSegment(input));
    }

    /**
     * @param segment an in between 0 and {@link #getTotalSegments()} - 1
     * @return Experiment the segment is allocated to, may be null
     */
    public Experiment getExperiment(final int segment) {
        checkElementIndex(segment, allocationMap.length, "segment");
        final Experiment experiment = allocationMap[segment];
        logger.debug("segment {} belongs to experiment {}", segment, experiment);
        return experiment;
    }

    /**
     * Maps a primary unit value (e.g. user ID, a GUID cookie, etc.) to one of the valid segments.
     * This is done deterministically (the underlying code utilizes hashing algo).
     * @param input input map must contain an entry with the value of {@link #unit} as a key
     * @return int in the range of 0 .. totalSegments-1
     */
    public int getSegment(final Map<String, ?> input) {
        checkArgument(input.containsKey(unit),
                "Supplied input does not have a value for '%s' (primary unit of namespace %s)", unit, name);
        final Object unitVal = input.get(unit);
        final Long segment = new RandomInteger(0, getTotalSegments()-1, unitVal).eval();
        logger.debug("Unit {} hashes to segment {}", unitVal, segment);
        return segment.intValue();
    }

    /**
     * @return default experiment
     */
    public Experiment getDefaultExperiment() {
        return defaultExperiment;
    }

    private Collection<Integer> allocateSegments(final int segments, final String expName) {
        checkArgument(segments <= availableSegments.size(),
                "Experiment %s requests %s segments but only %s (out of %s) are available",
                name, segments, availableSegments.size(), getTotalSegments());
        final List<Integer> usedSegments = new Sample<>(new ArrayList<>(availableSegments), segments, expName).eval();
        availableSegments.removeAll(usedSegments);
        return usedSegments;
    }

}
