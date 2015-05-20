package com.glassdoor.planout4j.util;

import java.util.Collection;

/**
 * Detects whether a given object implements {@link Collection}.
 * The non-trivial implementation is necessary to support different JavaScript engines.
 */
public interface CollectionDetector {

    boolean isCollection(Object o);

    Collection<?> extractCollection(Object o);

    CollectionDetector DEFAULT = new CollectionDetector() {
        @Override
        public boolean isCollection(final Object o) {
            return o instanceof Collection;
        }
        @Override
        public Collection<?> extractCollection(final Object o) {
            return (Collection<?>)o;
        }
    };

}
