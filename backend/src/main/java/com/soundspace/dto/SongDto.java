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
        Long storageKeyId
) {

    public static SongDto toDto(Song song) {
        Album album = song.getAlbum();

        return new SongDto(
                song.getId(),
                song.getTitle(),
                song.getAuthor().getId(),
                song.getAuthor().getLogin(),
                album == null ? null : album.getId(),
                song.getGenres() == null ? List.of() : song.getGenres().stream().map(Genre::toString).toList(),
                song.getPubliclyVisible() != null && song.getPubliclyVisible(),
                song.getCreatedAt().toString(),
                song.getCoverStorageKey().getId()
        );
    }

}
