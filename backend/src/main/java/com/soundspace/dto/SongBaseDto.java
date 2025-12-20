package com.soundspace.dto;

import com.soundspace.dto.projection.SongBaseProjection;
import com.soundspace.entity.Song;
import lombok.Builder;

@Builder
public record SongBaseDto(
        Long id,
        String title,
        Long authorId,
        String authorUsername,
        Long coverStorageKeyId,
        String createdAt
) {
    public static SongBaseDto toDto(Song song) {
        return new SongBaseDto(
                song.getId(),
                song.getTitle(),
                song.getAuthor().getId(),
                song.getAuthor().getLogin(),
                song.getCoverStorageKey().getId(),
                song.getCreatedAt().toString()
        );
    }

    public static SongBaseDto toDto(SongBaseProjection p) {

        return new SongBaseDto(
                p.getId(),
                p.getTitle(),
                p.getAuthorId(),
                p.getAuthorUsername(),
                p.getCoverStorageKeyId(),
                p.getCreatedAt()
        );
    }
}