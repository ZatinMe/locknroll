package com.locknroll.exception;

/**
 * Exception thrown when trying to create a workflow instance that already exists
 */
public class WorkflowInstanceAlreadyExistsException extends RuntimeException {
    
    public WorkflowInstanceAlreadyExistsException(String message) {
        super(message);
    }
    
    public WorkflowInstanceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
