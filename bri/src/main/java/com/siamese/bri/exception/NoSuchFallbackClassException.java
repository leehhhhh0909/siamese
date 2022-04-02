package com.siamese.bri.exception;

public class NoSuchFallbackClassException extends RuntimeException {

    public NoSuchFallbackClassException() {
        super();
    }

    public NoSuchFallbackClassException(String message) {
        super(message);
    }

    public NoSuchFallbackClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchFallbackClassException(Throwable cause) {
        super(cause);
    }
}
