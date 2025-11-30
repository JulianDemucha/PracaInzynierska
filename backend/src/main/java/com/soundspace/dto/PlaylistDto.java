package com.soundspace.dto;

import com.soundspace.entity.Playlist;

public record PlaylistDto (
        Long id,
        String name,
        Long creatorId,
        Boolean publiclyVisible,
        String createdAt,
        String updatedAt
){
    public static PlaylistDto toDto(Playlist playlist) {
        return new PlaylistDto(
                playlist.getId(),
                playlist.getName(),
                playlist.getCreator().getId(),
                playlist.getPubliclyVisible(),
                playlist.getCreatedAt().toString(),
                playlist.getUpdatedAt().toString()
        );
    }
}
