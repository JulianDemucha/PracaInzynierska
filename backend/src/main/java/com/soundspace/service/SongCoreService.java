package com.soundspace.service;

import com.soundspace.dto.SongDto;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.entity.Album;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.AlbumNotFoundException;
import com.soundspace.exception.SongNotFoundException;
import com.soundspace.exception.StorageFileNotFoundException;
import com.soundspace.repository.AlbumRepository;
import com.soundspace.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongCoreService {
    public final SongRepository songRepository;
    private final AppUserService appUserService;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;

    public Song getSongById(Long id) {
        return songRepository.findById(id).orElseThrow(
                () -> new SongNotFoundException(id)
        );
    }

    public SongDto getSongDtoById(Long id) {
        return SongDto.toDto(getSongById(id));
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

    public void deleteSongById(Long id, String email) {
        Song song = getSongById(id);
        try{
        if (!song.getAuthor().getId().equals(getSongById(id).getAuthor().getId()))
            throw new AccessDeniedException("Requestujacy uzytkownik nie jest wlascicielem piosennki");

        songRepository.delete(song);

            storageService.delete(song.getAudioStorageKey());
            storageService.delete(song.getCoverStorageKey());
        } catch (IOException e){
            log.info(e.getMessage());
            throw new StorageFileNotFoundException(e.getMessage());
        } catch (AccessDeniedException e){
            log.info(e.getMessage());
            throw e;
        }
    }
}
