package com.soundspace.service;

import com.soundspace.config.ApplicationConfigProperties;
import com.soundspace.dto.AppUserDto;
import com.soundspace.entity.*;
import com.soundspace.enums.Sex;
import com.soundspace.exception.StorageException;
import com.soundspace.exception.UserNotFoundException;
import com.soundspace.repository.*;
import com.soundspace.dto.request.AppUserUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final ImageService imageService;
    private final StorageKeyRepository storageKeyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final PlaylistEntryRepository playlistEntryRepository;
    private final PlaylistRepository playlistRepository;
    private final SongReactionRepository songReactionRepository;
    private final SongViewRepository songViewRepository;
    private final ApplicationConfigProperties.MediaConfig.AvatarConfig avatarConfig;

    public AppUserDto getAppUser(Long userId) {
        return appUserRepository.findById(userId).map(AppUserDto::toDto).orElseThrow();
    }

    public AppUserDto getAuthenticatedUser(UserDetails userDetails) {

        AppUser user = appUserRepository.findByEmail(userDetails.getUsername()) // getUsername zwraca email
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User from token has NOT been found in the database: " + userDetails.getUsername() //email
                ));
        return AppUserDto.toDto(user);
    }

    @Transactional
    public AppUserDto update(AppUserUpdateRequest request, UserDetails userDetails) {

        // user details subject to email zamiast username -> getUsername() zwraca email
        AppUser updatedUser = appUserRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!isNullOrIsBlank(request.username())) {
            if (request.username().length() < 3 || request.username().length() > 16) {
                throw new ResponseStatusException((HttpStatus.CONFLICT),
                        "Login musi mieć pomiędzy 3 a 16 znaków");
            }

                /*
                jak login nie rowna sie aktualnemu loginowi, sprawdzamy czy
                user z nowym dostarczonym loginem juz istnieje
                */

            if (!updatedUser.getLogin().equals(request.username())) {
                if (appUserRepository.existsByLogin(request.username())) {
                    throw new ResponseStatusException((HttpStatus.CONFLICT),
                            "Nazwa użytkownika " + request.username() + " jest zajęta");
                }
            }

            updatedUser.setLogin(request.username());
        }

        if (!isNullOrIsBlank(request.email())) {
            if (!updatedUser.getEmail().equals(request.email())) {
                if (appUserRepository.existsByEmail(request.email())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Email " + request.email() + " jest zajęty");
                }
                updatedUser.setEmail(request.email());
            }
        }

        if (!isNullOrIsBlank(request.password())) {
            if (request.password().length() < 8 || request.password().length() > 24) {
                throw new ResponseStatusException((HttpStatus.CONFLICT),
                        "Hasło musi mieć pomiędzy 8 a 24 znaki");
            }
            updatedUser.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (request.bio() != null) {
            updatedUser.setBio(request.bio());
        }

        if (!isNullOrIsBlank(request.sex())) {
            try {
                Sex newSex = Sex.valueOf(request.sex());
                updatedUser.setSex(newSex);

            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Płeć '" + request.sex() + "' nie istnieje / nie jest wspierana przez system");
            }
        }

        // upload avatara i usuniecie poprzedniego
        MultipartFile avatar = request.avatarImageFile();
        if (avatar != null && !avatar.isEmpty())
            updatedUser = processAndSaveNewAvatar(avatar, updatedUser);


        // docelowy zapis update, jezeli avatar jest pusty
        appUserRepository.save(updatedUser);

        return AppUserDto.toDto(updatedUser);

    }

    // BULK DELETE -> 13 zapytań do bazy (jedno leci na komentarze, ktore prawdopodobnie zostana calkiem usuniete)
    @Transactional
    public void deleteUser(String requesterEmail) {
        AppUser appUser = getUserByEmail(requesterEmail);
        Long appUserId = appUser.getId();


        /// usuniecie wszystkich istniejacych w bazie refreshTokenow usera
        refreshTokenRepository.deleteAllByAppUserId(appUserId);


        /// usunięcie piosenek usera ze wszystkich playlist, usunięcie wszystkich piosenek z playlist usera i usunięcie playlist usera

        // zapisanie playlist do naprawienia po usunieciu piosenek usera (beda dziury w pozycjach)
        List<Long> playlistsToRepair = playlistEntryRepository.findPlaylistIdsToRepair(appUserId);

        // usuniecie songow usera ze wszystkich playlist w ktorych sa jak i wszystkie piosenki z jego wlasnych playlist
        playlistEntryRepository.deleteEntriesBySongAuthorId(appUserId);

        // naprawa playlist po usunieciu songow
        playlistEntryRepository.renumberPlaylists(playlistsToRepair);

        // usunięcie wszystkich playlist usera
        playlistRepository.deleteAllByCreatorId(appUserId);


        /// usuniecie wszystkich reakcji usera i reakcji dotyczacych piosenek usera
        songReactionRepository.deleteAllRelatedToUser(appUserId);

        /// odpiecie usera od encji jego wyswietlen
        songViewRepository.detachUserFromViews(appUserId);

        /// usunięcie wszystkich piosenek i następnie albumów usera

        // usuniecie wszystkich songow usera
        songRepository.deleteAllByAuthorId(appUserId);

        // usuniecie wszystkich albumow usera
        albumRepository.deleteAllByAuthorId(appUserId);


        /// usuniecie samego usera
        appUserRepository.delete(appUser);
        appUserRepository.flush();
        SecurityContextHolder.clearContext(); // wyczyszczenie kontekstu jic

        /// usunięcie kluczy do plików i na samym końcu samych plików powiązanych z userem []

        // usuniecie wszystkich storageKeys powiazanych z userem
        storageKeyRepository.deleteAllByUserId(appUserId);

        // usuniecie wszystkich plikow powiazanych bezposrednio z userem (pliki piosenek, okladki albumow, piosenek, playlist itp.)
        storageService.deleteAllUserFiles(appUserId);
        /// ]

    }


    ///////////////////////////// HELPERS /////////////////////////////

    private AppUser processAndSaveNewAvatar(MultipartFile avatar, AppUser updatedUser) {
        Path tmpAvatar = null;
        StorageKey previous = updatedUser.getAvatarStorageKey(); // może być placeholder

        try {
            // resize/convert -> ProcessedImage
            var processed = imageService.resizeImageAndConvert(avatar, avatarConfig.width(), avatarConfig.height(), avatarConfig.targetExtension(), avatarConfig.quality());

            // zapis do temp file
            tmpAvatar = Files.createTempFile("avatar-", "." + avatarConfig.targetExtension());
            Files.write(tmpAvatar, processed.bytes());

            // zapis do storage
            String storageKey = storageService.saveFromPath(tmpAvatar, updatedUser.getId(), avatarConfig.targetExtension(), avatarConfig.directory());
            log.info("Zapisano avatar do storage: {}", storageKey);

            // zapis encji StorageKey
            StorageKey sk = new StorageKey();
            sk.setKey(storageKey);
            sk.setMimeType(processed.contentType());
            sk.setSizeBytes(processed.bytes().length);
            sk = storageKeyRepository.save(sk);

            // docelowy zapis update
            updatedUser.setAvatarStorageKey(sk);
            updatedUser = appUserRepository.save(updatedUser);

            // usuniecie starego avatara (StorageKey w db i pliku w /data/ o ile nie jest default avatarem)
            cleanUpOldAvatar(previous);

            return updatedUser;

            //todo rzucic tu jakies custom exceptiony i handlowac na http status

        } catch (IOException e) {
            throw new StorageException("Błąd zapisu pliku avatara", e);

        } finally {
            if (tmpAvatar != null) {
                try {
                    Files.deleteIfExists(tmpAvatar);
                } catch (IOException ex) {
                    log.warn("Nie udalo sie usunac temp file avatara", ex);
                }
            }
        }
    }

    private void cleanUpOldAvatar(StorageKey previous) {
        // usuwanie poprzedniego avatara jezeli to nie devault
        if (previous != null && !previous.getId().equals(avatarConfig.defaultAvatarId())) {
            try {
                storageService.delete(previous.getKey());
            } catch (Exception ex) {
                log.warn("Nie udało się usunąć pliku starego avatara z storage: {}", previous.getKey(), ex);
            }
            try {
                storageKeyRepository.delete(previous);
            } catch (Exception ex) {
                log.warn("Nie udało się usunąć wpisu StorageKey starego avatara: id={}", previous.getId(), ex);
            }
        }
    }

    private Boolean isNullOrIsBlank(String s) {
        if (s == null) {
            return true;
        } else return s.isBlank();
    }

    public AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("Nie znaleziono użytkownika o emailu: " + email));
    }

    public AppUser getUserById(Long id) {
        return appUserRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("Nie znaleziono użytkownika o id: " + id));
    }



}
