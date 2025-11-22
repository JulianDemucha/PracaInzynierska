package com.soundspace.dto;

import com.soundspace.entity.Song;
import lombok.Builder;

import java.util.List;

@Builder
public record SongDto(
        Long id,
        String title,
        Long authorId,
        Long albumId,
        List<String> genres,
        boolean publiclyVisible,
        String createdAt,
        String coverStorageKey //poki co zostawiam bo jakby byl globalny endpoint do odczytu obrazow to sie przyda w odpowiedzi
){


}
