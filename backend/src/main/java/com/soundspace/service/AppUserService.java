package com.soundspace.service;
import com.soundspace.dto.AppUserDto;
import com.soundspace.dto.mapper.AppUserMapper;
import com.soundspace.entity.AppUser;
import com.soundspace.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository repo;
    private final AppUserMapper appUserMapper;

    public ResponseEntity<AppUserDto> getAuthenticatedUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        AppUser user = repo.findByEmail(userDetails.getUsername()) // getUsername returns email
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User from token has NOT been found in the database: " + userDetails.getUsername() //email
                ));
        return ResponseEntity.ok(appUserMapper.toDto(user));
    }
}
