package com.globsest.documenttestitq.exception;


//Выбрасывается, когда запись в реестре утверждений уже существует.

public class RegistryAlreadyExistsException extends RuntimeException {

    public RegistryAlreadyExistsException(String message) {
        super(message);
    }

    public RegistryAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
