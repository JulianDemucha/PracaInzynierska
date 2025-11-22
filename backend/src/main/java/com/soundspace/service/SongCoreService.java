package com.soundspace.service;

import com.soundspace.dto.SongDto;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.entity.Song;
import com.soundspace.exception.SongNotFoundException;
import com.soundspace.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongCoreService {
    public final SongRepository songRepository;
    private final AppUserService appUserService;

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
                .authorId(song.getAuthor().getId())
                .title(song.getTitle())
                .albumId(albumId)
                .createdAt(song.getCreatedAt().toString())
                .coverStorageKey(song.getCoverStorageKey())
                .genres(song.getGenres().stream().map(Enum::toString).collect(Collectors.toList()))
                .publiclyVisible(song.getPubliclyVisible())
                .build();
    }

    public List<SongDto> getSongsByUserId(Long songsAuthorId, String userEmail) {
        List<SongProjection> songsProjection = songRepository.findSongsByUserNative(songsAuthorId);

        boolean isRequestingUserAuthorOfSongs = appUserService.getUserByEmail(userEmail).getId().equals(songsAuthorId);

        List<SongDto> songs = new java.util.ArrayList<>(songsProjection.stream().map(p -> SongDto.builder()
                .id(p.getId())
                .authorId(p.getAuthorId())
                .title(p.getTitle())
                .albumId(p.getAlbumId())
                .genres(p.getGenres())
                .publiclyVisible(p.getPubliclyVisible())
                .createdAt(p.getCreatedAt().toString())
                .coverStorageKey(p.getCoverStorageKey())
                .build()).toList());

        // usuwa piosenki z listy jezeli sa prywatne, a requestujacy user nie jest autorem piosenek
        if (!isRequestingUserAuthorOfSongs)
            songs.removeIf(song -> !song.publiclyVisible());

        return songs;
    }
}
