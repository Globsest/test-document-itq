package com.globsest.documenttestitq.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(String message) {
        super(message);
    }

    public DocumentNotFoundException(Long id) {
        super("Документ с id " + id + " не найден");
    }
}
