package com.soundspace.exception;

public class SongNotFoundException extends RuntimeException {
    public SongNotFoundException(Long id) {
        super(String.format("Song with id %s not found", id));
    }
}
