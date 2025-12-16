package com.soundspace.dto;

import com.soundspace.dto.projection.AlbumProjection;
import com.soundspace.entity.Album;
import com.soundspace.entity.StorageKey;
import lombok.Builder;

import java.util.List;
import java.util.Objects;

@Builder
public record AlbumDto(
        Long id,
        String title,
        String description,
        Long authorId,
        String authorUsername,
        boolean publiclyVisible,
        String createdAt,
        List<String> genres,
        Long coverStorageKeyId
) {

    public static AlbumDto toDto(Album album) {
        StorageKey coverStorageKey = album.getCoverStorageKey();

        return new AlbumDto(
                album.getId(),
                album.getTitle(),
                album.getDescription(),
                album.getAuthor().getId(),
                album.getAuthor().getLogin(),
                album.getPubliclyVisible(),
                album.getCreatedAt().toString(),
                album.getGenres().stream().map(Objects::toString).toList(),
                coverStorageKey == null ? 6767L : coverStorageKey.getId()
                // 6767 to id placeholdera insertowanego w migracji
        );
    }

    public static AlbumDto toDto(AlbumProjection p) {

        return new AlbumDto(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getAuthorId(),
                p.getAuthorLogin(),
                p.getPubliclyVisible(),
                p.getCreatedAt().toString(),
                p.getGenres().stream().map(Objects::toString).toList(),
                p.getCoverStorageKeyId()
        );
    }
}
