package com.nightsky.keycache.factory.exception;

/**
 *
 * @author Chris
 */
public class KeyPairCreationException extends RuntimeException {

    public KeyPairCreationException() {
        super();
    }

    public KeyPairCreationException(String message) {
        super(message);
    }

    public KeyPairCreationException(Throwable t) {
        super(t);
    }

    public KeyPairCreationException(String message, Throwable t) {
        super(message, t);
    }

}
