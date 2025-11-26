package com.soundspace.service;
import com.soundspace.dto.AppUserDto;
import com.soundspace.dto.mapper.AppUserMapper;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.StorageKey;
import com.soundspace.enums.Sex;
import com.soundspace.exception.ImageProcessingException;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.dto.request.AppUserUpdateRequest;
import com.soundspace.repository.StorageKeyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final ImageService imageService;
    private final StorageKeyRepository storageKeyRepository;

    private static final Long DEFAULT_AVATAR_IMAGE_STORAGE_KEY_ID = 6767L;
    private static final String AVATARS_TARGET_DIRECTORY = "users/avatars";
    private static final String AVATAR_OUTPUT_EXTENSION = "jpg";
    private static final int AVATAR_WIDTH = 600;
    private static final int AVATAR_HEIGHT = 600;
    private static final double AVATAR_QUALITY = 0.80;

    public AppUserDto getAppUser(Long userId) {
        return appUserRepository.findById(userId).map(AppUserDto::toDto).orElseThrow();
    }

    public AppUserDto getAuthenticatedUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        AppUser user = appUserRepository.findByEmail(userDetails.getUsername()) // getUsername zwraca email
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User from token has NOT been found in the database: " + userDetails.getUsername() //email
                ));
        return AppUserDto.toDto(user);
    }

    @Transactional
    public ResponseEntity<?> updateUser(AppUserUpdateRequest request, Authentication authentication) {
        try {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // user details subject to email zamiast username -> getUsername() zwraca email
            Optional<AppUser> existingUser = appUserRepository.findByEmail(userDetails.getUsername());

            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            AppUser updatedUser = existingUser.get();

            if (!isNullOrIsBlank(request.username())) {
                if (request.username().length() < 3 || request.username().length() > 16) {
                    throw new ResponseStatusException((HttpStatus.CONFLICT),
                            "Login musi mieć pomiędzy 3 a 16 znaków");
                }

                /*
                jak login nie rowna sie aktualnemu loginowi, sprawdzamy czy
                user z nowym dostarczonym loginem juz istnieje
                */

                if(!updatedUser.getLogin().equals(request.username())) {
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

            if(request.bio() != null) {
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

            // upload avatara
            MultipartFile avatar = request.avatarImageFile();
            if (avatar != null && !avatar.isEmpty()) {
                Path tmpAvatar = null;
                StorageKey previous = updatedUser.getAvatarStorageKey(); // może być placeholder

                try {
                    // resize/convert -> ProcessedImage
                    var processed = imageService.resizeImageAndConvert(avatar, AVATAR_WIDTH, AVATAR_HEIGHT, AVATAR_OUTPUT_EXTENSION, AVATAR_QUALITY);

                    // zapis do temp file
                    tmpAvatar = Files.createTempFile("avatar-", "." + AVATAR_OUTPUT_EXTENSION);
                    Files.write(tmpAvatar, processed.bytes());

                    // zapis do storage
                    String storageKey = storageService.saveFromPath(tmpAvatar, updatedUser.getId(), AVATAR_OUTPUT_EXTENSION, AVATARS_TARGET_DIRECTORY);
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

                    // usuwanie poprzedniego avatara jezeli to nie devault
                    if (previous != null && !previous.getId().equals(DEFAULT_AVATAR_IMAGE_STORAGE_KEY_ID)) {
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

                    //todo rzucic tu jakies custom endpointy i handlowac na http status
                } catch (IllegalArgumentException | ImageProcessingException e) {
                    log.warn("Walidacja/processing obrazu avatara nie powiodla się: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid avatar image: " + e.getMessage());
                } catch (IOException e) {
                    log.error("Błąd I/O podczas zapisu avatara", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving avatar image");
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

            appUserRepository.save(updatedUser);

            return ResponseEntity.ok(AppUserDto.toDto(updatedUser));

        } catch (UsernameNotFoundException e) {
            log.debug("UpdateUser: Username not found");
            return ResponseEntity.badRequest().body("Username not found");

        } catch (Exception e) {
            log.error("UpdateUser: error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    private Boolean isNullOrIsBlank(String s) {
        if (s == null) {
            return true;
        } else return s.isBlank();
    }

    public AppUser getUserByEmail(String email) {
        return appUserRepository.findByEmail(email).orElseThrow();
    }

    public AppUser getUserById(Long id) {
        return appUserRepository.findById(id).orElseThrow();
    }

}
