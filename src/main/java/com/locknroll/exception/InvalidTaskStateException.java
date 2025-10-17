package com.locknroll.exception;

/**
 * Exception thrown when task state is invalid
 */
public class InvalidTaskStateException extends RuntimeException {
    
    public InvalidTaskStateException(String message) {
        super(message);
    }
    
    public InvalidTaskStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
