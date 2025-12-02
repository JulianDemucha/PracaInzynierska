package com.soundspace.dto;

import com.soundspace.entity.Playlist;

public record PlaylistDto(
        Long id,
        String title,
        Long creatorId,
        String creatorUsername,
        Boolean publiclyVisible,
        String createdAt,
        String updatedAt,
        Long coverStorageKeyId,
        int songsCount
) {
    public static PlaylistDto toDto(Playlist playlist) {
        int songsCount = playlist.getSongs() == null ? 0 : playlist.getSongs().size();

        return new PlaylistDto(
                playlist.getId(),
                playlist.getName(),
                playlist.getCreator().getId(),
                playlist.getCreator().getLogin(),
                playlist.getPubliclyVisible(),
                playlist.getCreatedAt().toString(),
                playlist.getUpdatedAt().toString(),
                playlist.getCoverStorageKey().getId(),
                songsCount
        );
    }
}
