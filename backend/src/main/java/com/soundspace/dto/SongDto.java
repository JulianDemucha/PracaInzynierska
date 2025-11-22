package com.soundspace.dto;

import com.soundspace.entity.Song;
import com.soundspace.enums.Genre;
import lombok.Builder;

import java.util.List;

@Builder
public record SongDto(
        Long id,
        String title,
        Long authorId,
        Long albumId,
        List<String> genres,
        boolean publiclyVisible,
        String createdAt,
        String coverStorageKey //poki co zostawiam bo jakby byl globalny endpoint do odczytu obrazow to sie przyda w odpowiedzi
){

    public static SongDto toDto(Song song) {
        return new SongDto(
                song.getId(),
                song.getTitle(),
                song.getAuthor().getId(),
                song.getAlbum().getId(),
                song.getGenres().stream().map(Genre::toString).toList(),
                song.getPubliclyVisible(),
                song.getCreatedAt().toString(),
                song.getCoverStorageKey()
        );
    }

}
