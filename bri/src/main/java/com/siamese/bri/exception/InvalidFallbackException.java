package com.siamese.bri.exception;

public class InvalidFallbackException extends RuntimeException {
    public InvalidFallbackException() {
        super();
    }

    public InvalidFallbackException(String message) {
        super(message);
    }


    public InvalidFallbackException(String message, Throwable cause) {
        super(message, cause);
    }


    public InvalidFallbackException(Throwable cause) {
        super(cause);
    }
}
