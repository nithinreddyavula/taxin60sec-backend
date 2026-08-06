package com.taxin60sec.backend.storage;

/**
 * Thrown whenever a storage operation (store, fetch, delete) fails,
 * or a requested stored resource cannot be located.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}