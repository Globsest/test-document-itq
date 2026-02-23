package com.globsest.documenttestitq.exception;

public class ApprovalRegistryException extends RuntimeException {

    public ApprovalRegistryException(String message) {
        super(message);
    }

    public ApprovalRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
