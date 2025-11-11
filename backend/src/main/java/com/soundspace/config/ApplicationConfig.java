package com.soundspace.config;

import com.soundspace.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AppUserRepository repo;

    // domyslnie spring security uwaza username jako subject do identufikowania uzytkownikow
    // tu zmieniam zeby loadByUsername szukalo uzytkownika po emailu
    // (AppUser : 72)
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user with email " + email + " not found"));
    }
}