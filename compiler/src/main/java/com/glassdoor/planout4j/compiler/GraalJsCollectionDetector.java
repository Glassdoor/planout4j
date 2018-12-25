package com.glassdoor.planout4j.compiler;

import com.glassdoor.planout4j.util.CollectionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class GraalJsCollectionDetector implements CollectionDetector {

    private static final Logger LOG = LoggerFactory.getLogger(GraalJsCollectionDetector.class);

    private static Class<?> SOM_CL;
    private static Method CONVERT;
    private static Method IS_ARRAY;
    private static Method VALUES;

    static {
        try {
            SOM_CL = Class.forName("org.graalvm.polyglot.Value");
            CONVERT = SOM_CL.getMethod("asValue", Object.class);
            IS_ARRAY = SOM_CL.getMethod("hasArrayElements");
            VALUES = SOM_CL.getMethod("as", Class.class);
        } catch (Exception e) {
            LOG.debug("Did *not* detect Graal JS engine, must be running on java 8 or 7", e);
            SOM_CL = null;
        }
    }

    public static final GraalJsCollectionDetector INSTANCE = new GraalJsCollectionDetector();

    private GraalJsCollectionDetector() {
    }

    public boolean isSupported() {
        return SOM_CL != null;
    }

    @Override
    public boolean isCollection(final Object o) {
        try {
            if (o == null) {
                return false;
            }
            final Object value = CONVERT.invoke(null, o);
            return (Boolean) IS_ARRAY.invoke(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unexpected reflection error while interrogating a polyglot instance", e);
        }
    }

    @Override
    public Collection<?> extractCollection(final Object o) {
        try {
            final Object value = CONVERT.invoke(null, o);
            return (Collection<?>) VALUES.invoke(value, List.class);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unexpected reflection error while interrogating a polyglot instance", e);
        }
    }
}
