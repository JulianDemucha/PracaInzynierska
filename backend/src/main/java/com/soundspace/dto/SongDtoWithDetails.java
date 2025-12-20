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
public record SongDtoWithDetails(
        Long id,
        String title,
        Long authorId,
        String authorUsername,
        Long albumId,
        List<String> genres,
        boolean publiclyVisible,
        String createdAt,
        Long coverStorageKeyId,
        Integer likesCount,
        Integer dislikesCount,
        Long viewCount
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

    public static SongDtoWithDetails toDto(SongStatslessProjection p, SongStatistics stats) {
        return getSongDtoWithDetails(
                stats,
                p.getId(),
                p.getTitle(),
                p.getAuthorId(),
                p.getAuthorUsername(),
                p.getAlbumId(),
                p.getGenres(),
                p.getPubliclyVisible(),
                p.getCreatedAt(),
                p.getCoverStorageKeyId()
        );
    }

    // do dociagania nowych statystyk jak wynik jest cached
    public static SongDtoWithDetails toDto(Song song, SongStatistics stats) {
        Album album = song.getAlbum();
        return new SongDtoWithDetails(
                song.getId(),
                song.getTitle(),
                song.getAuthor().getId(),
                song.getAuthor().getLogin(),
                album == null ? null : album.getId(),
                song.getGenres() == null ? List.of() : song.getGenres().stream().map(Genre::toString).toList(),
                song.getPubliclyVisible(),
                song.getCreatedAt().toString(),
                song.getCoverStorageKey().getId(),
                stats != null ? stats.getLikesCount() : 0,
                stats != null ? stats.getDislikesCount() : 0,
                stats != null ? stats.getViewCount() : 0L
        );
    }

    // do dociagania nowych statystyk jak wynik jest cached
    public static SongDtoWithDetails refreshStatistics(SongProjectionWithDetails p, SongStatistics stats) {
        return getSongDtoWithDetails(
                stats,
                p.getId(),
                p.getTitle(),
                p.getAuthorId(),
                p.getAuthorUsername(),
                p.getAlbumId(),
                p.getGenres(),
                p.getPubliclyVisible(),
                p.getCreatedAt(),
                p.getCoverStorageKeyId()
        );
    }

    // tylko do pelnych songow ze sfetchowanymi statystykami
    public static SongDtoWithDetails toDto(Song song) {
        if (song == null) return null;

        var stats = song.getStatistics();

        int likes = (stats != null) ? stats.getLikesCount() : 0;
        int dislikes = (stats != null) ? stats.getDislikesCount() : 0;
        long views = (stats != null) ? stats.getViewCount() : 0L;

        return new SongDtoWithDetails(
                song.getId(),
                song.getTitle(),
                song.getAuthor().getId(),
                song.getAuthor().getLogin(),
                song.getAlbum() == null ? null : song.getAlbum().getId(),
                song.getGenres() == null ? List.of() : song.getGenres().stream().map(Genre::toString).toList(),
                song.getPubliclyVisible(),
                song.getCreatedAt().toString(),
                song.getCoverStorageKey().getId(),
                likes,
                dislikes,
                views
        );
    }

    public static SongDtoWithDetails toDto(SongProjectionWithDetails p) {
        return new SongDtoWithDetails(
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
                (p.getDislikesCount() == null ? 0 : p.getDislikesCount()),
                (p.getViewCount() == null ? 0 : p.getViewCount())
        );
    }

    // helper

    private static SongDtoWithDetails getSongDtoWithDetails(SongStatistics stats,
                                                            Long id,
                                                            String title,
                                                            Long authorId,
                                                            String authorUsername,
                                                            Long albumId,
                                                            List<String> genres,
                                                            boolean publiclyVisible,
                                                            Instant createdAt,
                                                            Long coverStorageKeyId) {
        return new SongDtoWithDetails(
                id,
                title,
                authorId,
                authorUsername,
                albumId,
                genres,
                publiclyVisible,
                createdAt == null ? null : createdAt.toString(),
                coverStorageKeyId,
                stats != null ? stats.getLikesCount() : 0,
                stats != null ? stats.getDislikesCount() : 0,
                stats != null ? stats.getViewCount() : 0L
        );
    }
}