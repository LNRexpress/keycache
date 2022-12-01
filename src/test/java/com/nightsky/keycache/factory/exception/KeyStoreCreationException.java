package com.nightsky.keycache.factory.exception;

/**
 *
 * @author Chris
 */
public class KeyStoreCreationException extends RuntimeException {

    public KeyStoreCreationException() {
        super();
    }

    public KeyStoreCreationException(String message) {
        super(message);
    }

    public KeyStoreCreationException(Throwable t) {
        super(t);
    }

    public KeyStoreCreationException(String message, Throwable t) {
        super(message, t);
    }

}
