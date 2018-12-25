package com.glassdoor.planout4j.compiler;

import com.glassdoor.planout4j.util.CollectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Helper to resolve differences between JS engine shipped with JDK7 (Rhino) and the one shipped with JDK8 (Nashorn).
 * More details here: http://stackoverflow.com/questions/22492641/java8-js-nashorn-convert-array-to-java-array
 * <br>Detects whether Nashorn is used and extracts lists accordingly.
 */
public class NashornJsCollectionDetector implements CollectionDetector {

    private static final Logger LOG = LoggerFactory.getLogger(NashornJsCollectionDetector.class);

    private static Class<?> SOM_CL;
    private static Method IS_ARRAY;
    private static Method VALUES;

    static {
        try {
            SOM_CL = Class.forName("jdk.nashorn.api.scripting.ScriptObjectMirror");
            IS_ARRAY = SOM_CL.getMethod("isArray");
            VALUES = SOM_CL.getMethod("values");
        } catch (Exception e) {
            LOG.debug("Did *not* detect Nashorn JS engine, must be running on java 7", e);
            SOM_CL = null;
        }
    }

    public static final NashornJsCollectionDetector INSTANCE = new NashornJsCollectionDetector();

    private NashornJsCollectionDetector() {
    }

    public boolean isSupported() {
        return SOM_CL != null;
    }

    @Override
    public boolean isCollection(final Object o) {
        try {
            return o != null && SOM_CL.isAssignableFrom(o.getClass()) && (Boolean)IS_ARRAY.invoke(o);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new IllegalStateException("Unexpected reflection error while interrogating a ScriptObjectMirror instance", e);
        }
    }

    @Override
    public Collection<?> extractCollection(final Object o) {
        try {
            return (Collection<?>)VALUES.invoke(o);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new IllegalStateException("Unexpected reflection error while interrogating a ScriptObjectMirror instance", e);
        }
    }
}
