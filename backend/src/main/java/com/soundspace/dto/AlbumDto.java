package com.soundspace.dto;

import com.soundspace.entity.Album;
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

        return new AlbumDto(
                album.getId(),
                album.getTitle(),
                album.getDescription(),
                album.getAuthor().getId(),
                album.getAuthor().getLogin(),
                album.getPubliclyVisible(),
                album.getCreatedAt().toString(),
                album.getGenres().stream().map(Objects::toString).toList(),
                album.getCoverStorageKey().getId()
        );
    }
}
