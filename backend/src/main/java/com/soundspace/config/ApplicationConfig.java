package com.soundspace.config;

import com.soundspace.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AppUserRepository repo;

    // domyslnie spring security uwaza username jako subject do identufikowania uzytkownikow
    // tu zmieniam zeby loadByUsername szukalo uzytkownika po emailu
    // (AppUser : 74)
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user with email " + email + " not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Tika tika() { return new Tika(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public ApplicationConfigProperties.JwtConfig jwtConfig(ApplicationConfigProperties config) {
        return config.jwt();
    }

    @Bean
    public ApplicationConfigProperties.StorageConfig storageConfig(ApplicationConfigProperties config) {
        return config.storage();
    }

    @Bean
    public ApplicationConfigProperties.ViewsConfig viewsConfig(ApplicationConfigProperties config) {
        return config.views();
    }

    @Bean
    public ApplicationConfigProperties.MediaConfig.AvatarConfig avatarConfig(ApplicationConfigProperties config) {
        return config.media().avatar();
    }

    @Bean
    public ApplicationConfigProperties.MediaConfig.CoverConfig coverConfig(ApplicationConfigProperties config) {
        return config.media().cover();
    }

    @Bean
    public ApplicationConfigProperties.MediaConfig.AudioConfig audioConfig(ApplicationConfigProperties config) {
        return config.media().audio();
    }

    @Bean
    public ApplicationConfigProperties.CookieConfig cookieConfig(ApplicationConfigProperties config) {
        return config.cookie();
    }


}