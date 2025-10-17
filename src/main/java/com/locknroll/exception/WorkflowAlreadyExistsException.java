package com.locknroll.exception;

/**
 * Exception thrown when trying to create a workflow that already exists
 */
public class WorkflowAlreadyExistsException extends RuntimeException {
    
    public WorkflowAlreadyExistsException(String message) {
        super(message);
    }
    
    public WorkflowAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
