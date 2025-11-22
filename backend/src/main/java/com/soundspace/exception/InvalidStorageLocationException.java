package com.soundspace.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // 403
public class InvalidStorageLocationException extends RuntimeException {

    public InvalidStorageLocationException(String message) {
        super(message);
    }

    public InvalidStorageLocationException(String key, String message) {
        super(String.format("Błąd dla storageKey [%s]: %s", key, message));
    }
}