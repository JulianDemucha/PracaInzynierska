package com.soundspace.service.song;

import com.soundspace.config.ApplicationConfigProperties;
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
import com.soundspace.service.user.AppUserService;
import com.soundspace.service.storage.ImageService;
import com.soundspace.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final ApplicationConfigProperties.MediaConfig.CoverConfig coverConfig;
    private final ApplicationConfigProperties.MediaConfig.AudioConfig audioConfig;
    private final ReactionService reactionService;


    public Song getSongById(Long id) {
        return songRepository.findById(id).orElseThrow(
                () -> new SongNotFoundException(id)
        );
    }

    public Song getReferenceById(Long id) {
        return songRepository.getReferenceById(id);
    }

    public SongDto getSong(Long songId, UserDetails userDetails) {
        SongDto song = getSongDtoById(songId);
        if (song.publiclyVisible()) return song;
        if (userDetails == null) {
            throw new AccessDeniedException("Musisz być zalogowany, aby zobaczyć ten utwór.");
        }
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
        AppUser requester = null;

        boolean isAdmin = false;
        if(requesterEmail != null) {
            requester = appUserService.getUserByEmail(requesterEmail);
            isAdmin = requester.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        if (requester != null && !(song.getAuthor().getId().equals(requester.getId()) || isAdmin))
            throw new AccessDeniedException("Requestujacy uzytkownik nie jest wlascicielem piosenki ani administratorem");


        reactionService.deleteAllBySongId(id);
        songRepository.delete(song);

        // storageService moze rzucic IOException lub StorageException
        try {
            StorageKey audioKey = song.getAudioStorageKey();
            if (audioKey != null && !audioKey.getId().equals(audioConfig.defaultAudioId()) && audioKey.getKeyStr() != null && !audioKey.getKeyStr().isBlank()) {
                try {
                    storageService.delete(audioKey.getKeyStr());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć pliku audio z storage: {}", audioKey.getKeyStr(), ex);
                    throw ex;
                }

                try {
                    storageKeyRepository.deleteById(audioKey.getId());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć rekordu storage_keys (song audio) id={}: {}", audioKey.getId(), ex.getMessage());
                }
            }

            StorageKey coverKey = song.getCoverStorageKey();
            if (song.getAlbum() == null && coverKey != null && !coverKey.getId().equals(coverConfig.defaultCoverId()) && coverKey.getKeyStr() != null && !coverKey.getKeyStr().isBlank()) {
                try {
                    storageService.delete(coverKey.getKeyStr());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć pliku cover z storage: {}", coverKey.getKeyStr(), ex);
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
                    coverConfig.width(),
                    coverConfig.height(),
                    coverConfig.quality(),
                    coverConfig.targetExtension(),
                    coverConfig.songDirectory(),
                    "cover"
            ));
        }

        if (request.title() != null) {
            updatedSong.setTitle(request.title());
        }

        if (request.publiclyVisible() != null && updatedSong.getAlbum() == null) {
            updatedSong.setPubliclyVisible(request.publiclyVisible());
        }

        songRepository.save(updatedSong);
        if (storageKeyToDelete != null) {
            imageService.cleanUpOldImage(storageKeyToDelete, "cover");
        }
        return SongDto.toDto(updatedSong);
    }

    public Page<SongDto> getFavouriteSongs(UserDetails userDetails, Pageable pageable) {
        Long userId = appUserService.getUserByEmail(userDetails.getUsername()).getId();
        return songRepository.findAllFavouriteByAppUserId(userId, pageable).map(SongDto::toDto);
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
