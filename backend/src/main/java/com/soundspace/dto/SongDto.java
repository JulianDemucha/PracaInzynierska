package com.soundspace.dto;

import com.soundspace.dto.projection.SongProjection;
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
        Long coverStorageKeyId,
        int likesCount,
        int dislikesCount
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
                song.getCoverStorageKey().getId(),
                song.getLikesCount(),
                song.getDislikesCount()
        );
    }

    public static SongDto toDto(SongProjection p) {
        return new SongDto(
                p.getId(),
                p.getTitle(),
                p.getAuthorId(),
                p.getAuthorUsername(),
                p.getAlbumId(),
                p.getGenres(),
                p.getPubliclyVisible(),
                p.getCreatedAt() == null ? null : p.getCreatedAt().toString(),
                p.getCoverStorageKeyId(),
                (p.getLikesCount() == null ? 0 : p.getLikesCount()),
                (p.getDislikesCount() == null ? 0 : p.getDislikesCount())
        );
    }

}
