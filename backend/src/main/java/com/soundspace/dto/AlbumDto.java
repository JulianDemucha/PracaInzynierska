package com.soundspace.dto;

import com.soundspace.entity.Album;
import lombok.Builder;

@Builder
public record AlbumDto(
        Long id,
        String title,
        String description,
        Long authorId,
        String authorName,
        boolean publiclyVisible,
        String createdAt,
        Long coverStorageKeyId
) {

    public static AlbumDto toDto(Album album) {

        String nameToShow = album.getAuthor().getLogin();

        return new AlbumDto(
                album.getId(),
                album.getTitle(),
                album.getDescription(),
                album.getAuthor().getId(),
                nameToShow,
                album.getPubliclyVisible(),
                album.getCreatedAt().toString(),
                album.getCoverStorageKey().getId()
        );
    }
}
