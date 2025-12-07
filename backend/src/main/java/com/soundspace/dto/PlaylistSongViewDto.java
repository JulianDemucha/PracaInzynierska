package com.soundspace.dto;

import com.soundspace.dto.projection.PlaylistSongProjection;
import com.soundspace.entity.PlaylistEntry;
import com.soundspace.entity.Song;
import lombok.Builder;

import java.util.List;

@Builder
public record PlaylistSongViewDto(
        // todo upewnic sie pozniej ze nie ma niepotrzebnych wartosci tutaj nieuzywanych na frontcie
        Long id,
        String title,
        Long authorId,
        String authorUsername,
        Long albumId,
        List<String> genres,
        boolean publiclyVisible,
        String createdAt,
        Long coverStorageKeyId,
        int positionInPlaylist,
        Integer likesCount,
        Integer dislikesCount,
        Long viewCount
) {

    public static PlaylistSongViewDto toDto(PlaylistSongProjection psp) {
        return new PlaylistSongViewDto(
                psp.getId(),
                psp.getTitle(),
                psp.getAuthorId(),
                psp.getAuthorUsername(),
                psp.getAlbumId(),
                psp.getGenres(),
                psp.getPubliclyVisible(),
                psp.getCreatedAt().toString(),
                psp.getCoverStorageKeyId(),
                psp.getPosition(),
                psp.getLikesCount() == null ? 0 : psp.getLikesCount(),
                psp.getDislikesCount() == null ? 0 : psp.getLikesCount(),
                psp.getViewCount()
        );
    }

    // nie uzywac w listach, bo lazy loading
    public static PlaylistSongViewDto toDto(PlaylistEntry playlistEntry) {
        Song song = playlistEntry.getSong();
        Long albumId = song.getAlbum() == null ? null : song.getAlbum().getId();

        return new PlaylistSongViewDto(
                song.getId(),
                song.getTitle(),
                song.getAuthor().getId(),
                song.getAuthor().getLogin(),
                albumId,
                song.getGenres().stream().map(Object::toString).toList(),
                song.getPubliclyVisible(),
                song.getCreatedAt().toString(),
                song.getCoverStorageKey().getId(),
                playlistEntry.getPosition(),
                song.getLikesCount(),
                song.getDislikesCount(),
                song.getViewCount()
        );
    }

}
