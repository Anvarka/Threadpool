package org.itmo.java.threadpool;

public class LightExecutionException extends Exception {

    public LightExecutionException() {
        super();
    }

    public LightExecutionException(String message) {
        super(message);
    }

    public LightExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightExecutionException(Throwable cause) {
        super(cause);
    }

    protected LightExecutionException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
