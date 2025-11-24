package com.soundspace.dto;

import com.soundspace.entity.Album;
import com.soundspace.entity.Song;
import com.soundspace.enums.Genre;
import lombok.Builder;

import java.util.List;

@Builder
public record SongDto(
        Long id,
        String title,
        Long authorId,
        String authorUsername,
        Long albumId,
        List<String> genres,
        boolean publiclyVisible,
        String createdAt,
        String coverStorageKey //poki co zostawiam bo jakby byl globalny endpoint do odczytu obrazow to sie przyda w odpowiedzi
){

    public static SongDto toDto(Song song) {
        Album album = song.getAlbum();
        Long albumId = album == null ? null : album.getId();

        String usernameToShow = song.getAuthor().getLogin();

        return new SongDto(
                song.getId(),
                song.getTitle(),
                song.getAuthor().getId(),
                usernameToShow,
                albumId,
                song.getGenres().stream().map(Genre::toString).toList(),
                song.getPubliclyVisible(),
                song.getCreatedAt().toString(),
                song.getCoverStorageKey()
        );
    }

}
