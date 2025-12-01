package com.soundspace.dto;

import com.soundspace.entity.Playlist;
import com.soundspace.entity.PlaylistEntry;
import com.soundspace.entity.Song;

import java.util.List;

public record PlaylistDto(
        Long id,
        String name,
        Long creatorId,
        String creatorUsername,
        Boolean publiclyVisible,
        String createdAt,
//        String updatedAt,
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
//                playlist.getUpdatedAt().toString(),
                playlist.getCoverStorageKey().getId(),
                songsCount
        );
    }
}
