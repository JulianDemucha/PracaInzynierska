package com.soundspace.service;

import com.soundspace.dto.SongDto;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.dto.request.SongUpdateRequest;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.StorageKey;
import com.soundspace.exception.*;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.enums.Genre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongCoreService {
    private final SongRepository songRepository;
    private final AppUserService appUserService;
    private final StorageService storageService;
    private final StorageKeyRepository storageKeyRepository;
    private final ImageService imageService;

    private static final Long DEFAULT_COVER_IMAGE_STORAGE_KEY_ID = 6767L;
    private static final Long DEFAULT_AUDIO_STORAGE_KEY_ID = 5000L;
    private static final String COVER_TARGET_DIRECTORY = "songs/covers";
    private static final String COVER_TARGET_EXTENSION = "jpg";
    private static final int COVER_WIDTH = 1200;
    private static final int COVER_HEIGHT = 1200;
    private static final double COVER_QUALITY = 0.80;

    public Song getSongById(Long id) {
        return songRepository.findById(id).orElseThrow(
                () -> new SongNotFoundException(id)
        );
    }

    public SongDto getSong(Long songId, UserDetails userDetails) {
        SongDto song = getSongDtoById(songId);
        if (song.publiclyVisible()) return song;
        if (!appUserService.getUserByEmail(userDetails.getUsername()).getId().equals(song.authorId()))
            throw new AccessDeniedException("Brak dostępu do piosenki");

        return song;
    }

    public List<SongDto> getSongsByUserId(Long songsAuthorId, UserDetails userDetails) {
        if (userDetails == null)
            return songRepository.findPublicSongsByUserNative(songsAuthorId)
                    .stream()
                    .map(SongDto::toDto)
                    .toList();

        AppUser appUser = appUserService.getUserByEmail(userDetails.getUsername());
        if (appUser.getId().equals(songsAuthorId))
            return songRepository.findSongsByUserNative(songsAuthorId)
                    .stream()
                    .map(SongDto::toDto)
                    .toList();

        //else
        return songRepository.findPublicSongsByUserNative(songsAuthorId)
                .stream()
                .map(SongDto::toDto)
                .toList();
    }

    public List<SongDto> getSongsByGenre(String genreName, UserDetails userDetails) {
        try {
            Genre genre = Genre.valueOf(genreName.toUpperCase().trim());


            if (userDetails == null)
                return songRepository.findPublicByGenre(genre).stream()
                        .map(SongDto::toDto)
                        .toList();

            else return songRepository.findPublicOrOwnedByUserByGenre(
                            genre,
                            appUserService.getUserByEmail(userDetails.getUsername()).getId()
                    ).stream()
                    .map(SongDto::toDto)
                    .toList();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy gatunek: " + genreName);
        }

    }

    @Transactional
    public List<SongDto> getAllSongs(UserDetails userDetails) {
        if (userDetails == null)
            return songRepository.findAllPublic()
                    .stream()
                    .map(SongDto::toDto)
                    .toList();

        else return songRepository.findAllPublicOrOwnedByUser(
                        appUserService.getUserByEmail(userDetails.getUsername()).getId()).stream()
                        .map(SongDto::toDto)
                        .toList();
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

    @Transactional
    public SongDto update(Long songId, SongUpdateRequest request, UserDetails userDetails) { // @AuthenticationPrincipal userDetails jest NotNull
        Song updatedSong = getSongById(songId); //narazie wszytkie pola takie same, a pozniej beda zmieniane zeby zapisac po update

        AppUser user = appUserService.getUserByEmail(userDetails.getUsername());
        if (!updatedSong.getAuthor().getId().equals(user.getId()))
            throw new AccessDeniedException("Brak dostępu do edycji piosenki");

        MultipartFile coverFile = request.coverFile();
        StorageKey storageKeyToDelete = null;
        if (coverFile != null && !coverFile.isEmpty() && updatedSong.getAlbum() == null) {
            storageKeyToDelete = updatedSong.getCoverStorageKey();
            updatedSong.setCoverStorageKey(imageService.processAndSaveNewImage(
                    coverFile,
                    user,
                    COVER_WIDTH,
                    COVER_HEIGHT,
                    COVER_QUALITY,
                    COVER_TARGET_EXTENSION,
                    COVER_TARGET_DIRECTORY,
                    "cover"
            ));
        }

        if(request.title() != null){
            updatedSong.setTitle(request.title());
        }

        if(request.publiclyVisible() != null && updatedSong.getAlbum() == null){
            updatedSong.setPubliclyVisible(request.publiclyVisible());
        }

        songRepository.save(updatedSong);
        if(storageKeyToDelete != null){
            imageService.cleanUpOldImage(storageKeyToDelete, "cover");
        }
        return SongDto.toDto(updatedSong);
    }

    public List<SongDto> getTop10Liked(){
        return songRepository.findTopLikedSongsSinceCutoff(
                Instant.now().minusSeconds(60 * 60 * 24 * 7), //tydzien
                PageRequest.of(0, 10)
        ).stream().map(SongDto::toDto).toList();
    }


    /// HELPERY

    private SongDto getSongDtoById(Long id) {
        return SongDto.toDto(getSongById(id));
    }

    private List<SongDto> getSongsFromSongProjection(List<SongProjection> songsProjection) {
        return songsProjection.stream()
                .map(SongDto::toDto).toList();
    }

}
