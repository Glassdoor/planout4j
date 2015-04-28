package com.glassdoor.planout4j.config;

/**
 * Thrown whenever an attempt is made to load/process an invalid namespace configuration.
 * @author ernest.mishkin
 */
public class ValidationException extends Exception {

   private static final long serialVersionUID = -695226124545019225L;

   public ValidationException(final String message) {
        super(message);
    }

    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
