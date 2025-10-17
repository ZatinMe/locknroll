package com.locknroll.exception;

/**
 * Exception thrown when workflow configuration is invalid
 */
public class InvalidWorkflowException extends RuntimeException {
    
    public InvalidWorkflowException(String message) {
        super(message);
    }
    
    public InvalidWorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
