package com.soundspace.service.user;
import com.soundspace.config.ApplicationConfigProperties;
import com.soundspace.entity.AppUser;
import com.soundspace.enums.Role;
import com.soundspace.enums.Sex;
import com.soundspace.enums.UserAuthProvider;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.dto.request.AuthenticationRequest;
import com.soundspace.dto.request.RegisterRequest;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final AppUserRepository repo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final StorageKeyRepository storageKeyRepository;
    private final ApplicationConfigProperties.MediaConfig.AvatarConfig avatarConfig;

    public String register(RegisterRequest request) {
        // todo: zrobic customowe wyjatki i od razu je w klasach handlowac na 409 albo w global handlerze
        if (repo.existsByEmail(request.getEmail()) || repo.existsByLogin(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email lub login jest zajęty");
        }

        if (request.getPassword().length() < 8 || request.getPassword().length() > 24) {
            throw new ResponseStatusException((HttpStatus.CONFLICT),
                    "Hasło musi mieć pomiędzy 8 a 24 znaki");
        }

        if (request.getUsername().length() < 3 || request.getUsername().length() > 16) {
            throw new ResponseStatusException((HttpStatus.CONFLICT),
                    "Login musi mieć pomiędzy 3 a 16 znaków");
        }

        if (!request.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException((HttpStatus.CONFLICT),
                    "Niewłaściwy email");
        }

        var user = AppUser.builder()
                .login(request.getUsername())
                .sex(Sex.valueOf(request.getSex().toUpperCase().trim()))
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .createdAt(Instant.now())
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(false)
                .bio("")
                .comments(new ArrayList<>())
                // poki co defaultowy avatar
                .avatarStorageKey(storageKeyRepository.getReferenceById(avatarConfig.defaultAvatarId()))
                .build();

        repo.save(user);
        log.info("Zarejestrowano nowego użytkownika: id={}, username={}", user.getId(), user.getLogin());
        return jwtService.generateJwtToken(user);
    }


    public String authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            var user = repo.findByEmail(request.getEmail())
                    .orElseThrow();
            repo.save(user);
            return jwtService.generateJwtToken(user);
        }catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nieprawidłowy emnail lub hasło", e);
        }
    }



    // to nizej do debouncera ewentualnie

    public Boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    public Boolean existsByUsername(String username) {
        return repo.existsByLogin(username);
    }


}

