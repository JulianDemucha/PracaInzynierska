package com.soundspace.exception;

public class AlbumNotFoundException extends RuntimeException {
    public AlbumNotFoundException(Long id) {
        super(String.format("Album with id %s not found", id));
    }
}
