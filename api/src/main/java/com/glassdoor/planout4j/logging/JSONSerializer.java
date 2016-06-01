package com.glassdoor.planout4j.logging;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.glassdoor.planout4j.Experiment;
import com.google.common.base.Converter;
import com.google.common.base.MoreObjects;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Serializes an exposure logging record into JSON string.
 * Format of the record follows <a href="https://facebook.github.io/planout/docs/logging.html">planout example</a> with minor tweaks.
 * Sample record:<pre>
 {
     "event": "exposure",
     "timestamp": "2016-05-31T19:06:58.510Z",
     "namespace": "test_ns",
     "experiment": "def_exp",
     "checksum": "77341e05",
     "inputs": {
     "userid": 12345
     },
     "overrides": {
     },
     "params": {
     "specific_goal": true,
     "group_size": 1,
     "ratings_per_user_goal": 64,
     "ratings_goal": 64
     }
 }
 * </pre>
 * @author ernest_mishkin
 */
public class JSONSerializer extends Converter<LogRecord, String> {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final DateFormat ISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    static {
        ISO.setTimeZone(UTC);
    }
    private static final HashFunction CHECKSUM = Hashing.crc32();

    private final Gson gson;

    public JSONSerializer() {
        this(false);
    }

    public JSONSerializer(final boolean pretty) {
        final GsonBuilder builder = new GsonBuilder();
        if (pretty) {
            builder.setPrettyPrinting();
        }
        gson = builder.create();
    }

    @Override
    protected String doForward(final LogRecord record) {
        final JSONLoggingRecord jlr = new JSONLoggingRecord();
        jlr.event = "exposure";
        jlr.timestamp = ISO.format(new Date());
        jlr.namespace = record.namespace.getName();
        jlr.salt = record.namespace.nsConf.salt;
        final Experiment exp = MoreObjects.firstNonNull(record.namespace.getExperiment(), record.namespace.nsConf.getDefaultExperiment());
        jlr.experiment = exp.name;
        jlr.checksum = CHECKSUM.hashUnencodedChars(exp.def.getCopyOfScript().toString()).toString();
        jlr.inputs = record.input;
        jlr.overrides = record.overrides;
        jlr.params = record.namespace.getParams();
        return gson.toJson(jlr);
    }

    @Override
    protected LogRecord doBackward(final String s) {
        throw new IllegalStateException("Deserialization is not implemented");
    }


}
