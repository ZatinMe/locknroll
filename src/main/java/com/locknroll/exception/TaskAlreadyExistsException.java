package com.locknroll.exception;

/**
 * Exception thrown when trying to create a task that already exists
 */
public class TaskAlreadyExistsException extends RuntimeException {
    
    public TaskAlreadyExistsException(String message) {
        super(message);
    }
    
    public TaskAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
