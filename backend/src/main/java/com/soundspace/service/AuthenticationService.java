package com.soundspace.service;
import com.soundspace.entity.AppUser;
import com.soundspace.enums.Role;
import com.soundspace.enums.Sex;
import com.soundspace.enums.UserAuthProvider;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.security.dto.AuthenticationRequest;
import com.soundspace.security.dto.RegisterRequest;
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

    public String register(RegisterRequest request) {
        if (repo.existsByEmail(request.getEmail()) || repo.existsByLogin(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email or username already exists");
        }

        if (request.getPassword().length() < 8 || request.getPassword().length() > 24) {
            throw new ResponseStatusException((HttpStatus.CONFLICT),
                    "Password must be between 8 and 24 characters");
        }

        if (request.getUsername().length() < 3 || request.getUsername().length() > 16) {
            throw new ResponseStatusException((HttpStatus.CONFLICT),
                    "Username must be between 3 and 16 characters");
        }

        if (!request.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException((HttpStatus.CONFLICT),
                    "Invalid email format");
        }

        var user = AppUser.builder()
                .login(request.getUsername())
                .sex(Sex.valueOf(request.getSex()))
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .createdAt(Instant.now())
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(false)
                .bio("")
                .comments(new ArrayList<>())
                .build();

        repo.save(user);
        log.info("Registered new user: {}", user.getUsername());

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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password", e);
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

