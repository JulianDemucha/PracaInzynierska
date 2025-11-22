package com.soundspace.service;

import com.soundspace.dto.SongDto;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.entity.Album;
import com.soundspace.entity.Song;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.AlbumNotFoundException;
import com.soundspace.exception.SongNotFoundException;
import com.soundspace.repository.AlbumRepository;
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
    private final AlbumRepository albumRepository;

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

        List<SongDto> songs = getSongsFromSongProjection(songsProjection);

        // usuwa piosenki z listy jezeli sa prywatne, a requestujacy user nie jest autorem piosenek
        if (!isRequestingUserAuthorOfSongs)
            songs.removeIf(song -> !song.publiclyVisible());

        return songs;
    }

    public List<SongDto> getSongsByAlbumId(Long albumId, String userEmail) {
        List<SongProjection> songsProjection = songRepository.findSongsByAlbumNative(albumId);
        Album album = albumRepository.getAlbumById(albumId);
        if (album == null) throw new AlbumNotFoundException(albumId);

        // jezeli album jest prywatny i requestujacy user nie jest autorem albumu - throw
        if (!album.getPubliclyVisible() && !appUserService.getUserByEmail(userEmail).getId()
                .equals(album.getAuthor().getId()))
            throw new AccessDeniedException("Ten album jest prywatny. Brak uprawnień");

        return getSongsFromSongProjection(songsProjection);
    }

    private List<SongDto> getSongsFromSongProjection(List<SongProjection> songsProjection) {
        return new java.util.ArrayList<>(songsProjection.stream().map(p -> SongDto.builder()
                .id(p.getId())
                .authorId(p.getAuthorId())
                .title(p.getTitle())
                .albumId(p.getAlbumId())
                .genres(p.getGenres())
                .publiclyVisible(p.getPubliclyVisible())
                .createdAt(p.getCreatedAt().toString())
                .coverStorageKey(p.getCoverStorageKey())
                .build()).toList());
    }
}
