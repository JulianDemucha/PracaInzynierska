package com.soundspace.service;

import com.soundspace.dto.SongDto;
import com.soundspace.entity.Song;
import com.soundspace.exception.SongNotFoundException;
import com.soundspace.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongCoreService {
    public final SongRepository songRepository;

    public Optional<Song> getSongById(Long id) {
        return songRepository.findById(id);
    }

    public SongDto getSongDtoById(Long id) {
        Song song = songRepository.findById(id).orElseThrow(
                () -> new SongNotFoundException(id)
        );

        Long albumId = song.getAlbum() == null ? null : song.getAlbum().getId();

        return SongDto.builder()
                .id(song.getId())
                .authorUsername(song.getAuthor().getLogin())
                .title(song.getTitle())
                .albumId(albumId)
                .createdAt(song.getCreatedAt().toString())
                .audioStorageKey(song.getAudioStorageKey())
                .coverStorageKey(song.getCoverStorageKey())
                .genres(song.getGenres().stream().map(Enum::toString).collect(Collectors.toList()))
                .publiclyVisible(song.getPubliclyVisible())
                .build();
    }
}
