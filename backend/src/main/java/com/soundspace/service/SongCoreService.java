package com.soundspace.service;

import com.soundspace.dto.SongDto;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.entity.Album;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.StorageKey;
import com.soundspace.exception.*;
import com.soundspace.repository.AlbumRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.enums.Genre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongCoreService {
    public final SongRepository songRepository;
    private final AppUserService appUserService;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;
    private final StorageKeyRepository storageKeyRepository;

    private static final Long DEFAULT_COVER_IMAGE_STORAGE_KEY_ID = 6767L;
    private static final Long DEFAULT_AUDIO_STORAGE_KEY_ID = 5000L;

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
        return songsProjection.stream()
                .map(p -> new SongDto(
                        p.getId(),
                        p.getTitle(),
                        p.getAuthorId(),
                        p.getAuthorUsername(),
                        p.getAlbumId(),
                        p.getGenres(),
                        p.getPubliclyVisible(),
                        p.getCreatedAt() == null ? null : p.getCreatedAt().toString(),
                        p.getCoverStorageKeyId() // Long
                ))
                .collect(java.util.stream.Collectors.toList());
    }



    @Transactional
    public void deleteSongById(Long id, String requesterEmail) {
        Song song = getSongById(id);
        AppUser requester = appUserService.getUserByEmail(requesterEmail);

        if (requester == null || !song.getAuthor().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Requestujacy uzytkownik nie jest wlascicielem piosenki");
        }

        songRepository.delete(song);

        // storageService moze rzucic IOException lub StorageException
        try {
            StorageKey audioKey = song.getAudioStorageKey();
            if (audioKey != null && !audioKey.getId().equals(DEFAULT_AUDIO_STORAGE_KEY_ID) && audioKey.getKey() != null && !audioKey.getKey().isBlank()) {
                try {
                    storageService.delete(audioKey.getKey());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć pliku audio z storage: {}", audioKey.getKey(), ex);
                    throw ex;
                }

                try {
                    storageKeyRepository.deleteById(audioKey.getId());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć rekordu storage_keys (song audio) id={}: {}", audioKey.getId(), ex.getMessage());
                }
            }

            StorageKey coverKey = song.getCoverStorageKey();
            if (song.getAlbum() == null && coverKey != null && !coverKey.getId().equals(DEFAULT_COVER_IMAGE_STORAGE_KEY_ID) && coverKey.getKey() != null && !coverKey.getKey().isBlank()) {
                try {
                    storageService.delete(coverKey.getKey());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć pliku cover z storage: {}", coverKey.getKey(), ex);
                    throw ex;
                }
                try {
                    storageKeyRepository.deleteById(coverKey.getId());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć rekordu storage_keys (song cover) id={}: {}", coverKey.getId(), ex.getMessage());
                }
            }



        } catch (AccessDeniedException e) {
            log.info(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.info("Błąd podczas usuwania pliku/storage: {}", e.getMessage());
            throw new StorageException(e.getMessage());
        }
    }

    public List<SongDto> getSongsByGenre(String genreName) {
        try {
            Genre genre = Genre.valueOf(genreName.toUpperCase().trim());
            return songRepository.findAllByGenre(genre)
                    .stream()
                    .map(SongDto::toDto)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy gatunek: " + genreName);
        }
    }

    @Transactional
    public List<SongDto> getAllSongs() {
        return songRepository.findAllWithDetails()
                .stream()
                .map(SongDto::toDto)
                .toList();
    }
}
