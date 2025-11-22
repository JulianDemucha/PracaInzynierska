package com.soundspace.dto;

import com.soundspace.entity.Album;
import lombok.Builder;

@Builder
public record AlbumDto(
        Long id,
        String title,
        String description,
        Long authorId,
        boolean publiclyVisible,
        String createdAt
) {

    public static AlbumDto toDto(Album album) {
        return new AlbumDto(
                album.getId(),
                album.getTitle(),
                album.getDescription(),
                album.getAuthor().getId(),
                album.getPubliclyVisible(),
                album.getCreatedAt().toString()
        );
    }
}
