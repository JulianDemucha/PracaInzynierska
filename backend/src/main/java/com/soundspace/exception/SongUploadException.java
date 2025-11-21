package com.soundspace.exception;

public class SongUploadException extends RuntimeException {
    public SongUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
