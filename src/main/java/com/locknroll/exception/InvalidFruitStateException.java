package com.locknroll.exception;

/**
 * Exception thrown when fruit state is invalid for the requested operation
 */
public class InvalidFruitStateException extends RuntimeException {
    
    public InvalidFruitStateException(String message) {
        super(message);
    }
    
    public InvalidFruitStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
