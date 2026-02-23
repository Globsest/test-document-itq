package com.globsest.documenttestitq.exception;

import com.globsest.documenttestitq.entity.DocumentStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(DocumentStatus currentStatus, DocumentStatus targetStatus) {
        super("Недопустимый переход статуса: " + currentStatus + " -> " + targetStatus);
    }
}
