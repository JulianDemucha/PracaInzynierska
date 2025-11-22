package com.soundspace.service;
import com.soundspace.dto.AppUserDto;
import com.soundspace.dto.mapper.AppUserMapper;
import com.soundspace.entity.AppUser;
import com.soundspace.enums.Sex;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.dto.request.AppUserUpdateRequest;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService {
    private final AppUserRepository repo;
    private final AppUserMapper appUserMapper;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<AppUserDto> getAuthenticatedUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        AppUser user = repo.findByEmail(userDetails.getUsername()) // getUsername zwraca email
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User from token has NOT been found in the database: " + userDetails.getUsername() //email
                ));
        return ResponseEntity.ok(appUserMapper.toDto(user));
    }

    @Transactional
    public ResponseEntity<?> updateUser(AppUserUpdateRequest request, Authentication authentication) {
        try {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // user details subject to email zamiast username -> getUsername() zwraca email
            Optional<AppUser> existingUser = repo.findByEmail(userDetails.getUsername());

            if (existingUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            AppUser updatedUser = existingUser.get();

            if (!isNullOrIsBlank(request.username())) {
                if (request.username().length() < 3 || request.username().length() > 16) {
                    throw new ResponseStatusException((HttpStatus.CONFLICT),
                            "Username must be between 3 and 16 characters");
                }

                /*
                jak login nie rowna sie aktualnemu loginowi, sprawdzamy czy
                user z nowym dostarczonym loginem juz istnieje
                */

                if(!updatedUser.getLogin().equals(request.username())) {
                    if (repo.existsByLogin(request.username())) {
                        throw new ResponseStatusException((HttpStatus.CONFLICT),
                                "Username " + request.username() + "already exists");
                    }
                }

                updatedUser.setLogin(request.username());
            }

            if (!isNullOrIsBlank(request.email())) {
                if (!updatedUser.getEmail().equals(request.email())) {
                    if (repo.existsByEmail(request.email())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Email " + request.email() + " already exists");
                    }
                    updatedUser.setEmail(request.email());
                }
            }

            if (!isNullOrIsBlank(request.password())) {
                if (request.password().length() < 8 || request.password().length() > 24) {
                    throw new ResponseStatusException((HttpStatus.CONFLICT),
                            "Password must be between 8 and 24 characters");
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
                            "Sex '" + request.sex() + "' not supported / doesn't exist");
                }
            }

            repo.save(updatedUser);

            return ResponseEntity.ok(appUserMapper.toDto(updatedUser));

        } catch (UsernameNotFoundException e) {
            log.debug("UpdateUser: Username not found");
            return ResponseEntity.badRequest().body("Username not found");

        } catch (Exception e) {
            log.error("UpdateUser: Unexpected error while extracting username from JWT token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error while extracting username from JWT token");
        }
    }

    private Boolean isNullOrIsBlank(String s) {
        if (s == null) {
            return true;
        } else return s.isBlank();
    }

    public AppUser getUserByEmail(String email) {
        return repo.findByEmail(email).orElseThrow();
    }

    public AppUser getUserById(Long id) {
        return repo.findById(id).orElseThrow();
    }

}
