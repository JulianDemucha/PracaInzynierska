package com.soundspace.dto;

import com.soundspace.dto.projection.SongProjectionWithDetails;
import com.soundspace.dto.projection.SongStatslessProjection;
import com.soundspace.entity.Album;
import com.soundspace.entity.Song;
import com.soundspace.entity.SongStatistics;
import com.soundspace.enums.Genre;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record SongStatslessDto(
        Long id,
        String title,
        Long authorId,
        String authorUsername,
        Long albumId,
        List<String> genres,
        boolean publiclyVisible,
        String createdAt,
        Long coverStorageKeyId
) {
    // do dociagania nowych statystyk jak wynik jest cached
    public SongDtoWithDetails withStatistics(SongStatistics stats) {
        return new SongDtoWithDetails(
                this.id(),
                this.title(),
                this.authorId(),
                this.authorUsername(),
                this.albumId(),
                this.genres(),
                this.publiclyVisible(),
                this.createdAt(),
                this.coverStorageKeyId(),
                stats != null ? stats.getLikesCount() : 0,
                stats != null ? stats.getDislikesCount() : 0,
                stats != null ? stats.getViewCount() : 0L
        );
    }

    public static SongStatslessDto toDto(SongStatslessProjection p) {
        return new SongStatslessDto(
                p.getId(),
                p.getTitle(),
                p.getAuthorId(),
                p.getAuthorUsername(),
                p.getAlbumId(),
                p.getGenres(),
                p.getPubliclyVisible(),
                p.getCreatedAt().toString(),
                p.getCoverStorageKeyId()
        );
    }

    // do dociagania nowych statystyk jak wynik jest cached
    public static SongStatslessDto toDto(Song song) {
        Album album = song.getAlbum();
        return new SongStatslessDto(
                song.getId(),
                song.getTitle(),
                song.getAuthor().getId(),
                song.getAuthor().getLogin(),
                album == null ? null : album.getId(),
                song.getGenres() == null ? List.of() : song.getGenres().stream().map(Genre::toString).toList(),
                song.getPubliclyVisible(),
                song.getCreatedAt().toString(),
                song.getCoverStorageKey().getId()
        );
    }
}