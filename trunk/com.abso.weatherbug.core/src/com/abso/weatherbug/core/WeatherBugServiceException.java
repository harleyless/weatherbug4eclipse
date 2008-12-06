package com.abso.weatherbug.core;

/**
 * An exception thrown during a remote call to the WeatherBug APIs.
 */
public class WeatherBugServiceException extends Exception {

    /** Serial version identifier. */
    private static final long serialVersionUID = 933922717426456364L;

    /** Constructs a new exception with no specified detail message. */
    public WeatherBugServiceException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public WeatherBugServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception that wraps another exception.
     * 
     * @param cause
     *            the cause of this exception.
     */
    public WeatherBugServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception that wraps another exception.
     * 
     * @param message
     *            the associated message.
     * @param cause
     *            the cause of this exception.
     */
    public WeatherBugServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
