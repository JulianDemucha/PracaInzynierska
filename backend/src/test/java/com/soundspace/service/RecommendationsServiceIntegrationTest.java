package com.soundspace.service;

import com.soundspace.dto.SongBaseDto;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.SongReaction;
import com.soundspace.entity.StorageKey;
import com.soundspace.enums.*;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.repository.SongReactionRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.service.song.RecommendationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecommendationsServiceIntegrationTest {

    @Autowired
    private RecommendationsService recommendationsService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongReactionRepository songReactionRepository;

    @Autowired
    private StorageKeyRepository storageKeyRepository;

    private AppUser testUser;
    private StorageKey dummyKey;

    @BeforeEach
    void setUp() {
        dummyKey = new StorageKey();
        dummyKey.setKeyStr("test-key-" + UUID.randomUUID());
        dummyKey.setMimeType("image/jpeg");
        dummyKey.setSizeBytes(100L);
        dummyKey.setCreatedAt(Instant.now());
        storageKeyRepository.save(dummyKey);

        testUser = AppUser.builder()
                .email("musiclover@test.com")
                .login("musiclover")
                .passwordHash("pass")
                .role(Role.ROLE_USER)
                .sex(Sex.MALE)
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(Instant.now())
                .avatarStorageKey(dummyKey)
                .build();
        appUserRepository.save(testUser);
    }

    @Test
    @DisplayName("Powinien pozycjonować utwory zgodne z gustem wyżej niż utwory popularne (Backfill)")
    void shouldRankGenreMatchedSongsHigherThanBackfill() {
        Song likedRockSong = createSong("Classic Rock Hit", Genre.ROCK);
        createReaction(testUser, likedRockSong, ReactionType.LIKE);

        Song rockSong = createSong("Rock Song", Genre.ROCK);

        Song popSong = createSong("Pop Song", Genre.POP);

        UserDetails userDetails = User.withUsername(testUser.getEmail())
                .password("pass")
                .roles("USER")
                .build();

        List<SongBaseDto> recommendations = recommendationsService.getRecommendations(userDetails);

        assertThat(recommendations)
                .extracting(SongBaseDto::title)
                .containsSubsequence("Rock Song", "Pop Song");
    }

    @Test
    @DisplayName("Dla nowego użytkownika (bez historii) serwis powinien zwrócić pustą listę (Facade obsłuży fallback)")
    void shouldReturnEmptyListForColdStart() {
        UserDetails userDetails = User.withUsername(testUser.getEmail())
                .password("pass")
                .roles("USER")
                .build();

        List<SongBaseDto> recommendations = recommendationsService.getRecommendations(userDetails);

        assertThat(recommendations).isEmpty();
    }

    private Song createSong(String title, Genre genre) {
        Song song = new Song();
        song.setTitle(title);
        song.setAuthor(testUser);
        song.setPubliclyVisible(true);
        song.setGenres(List.of(genre));
        song.setAudioStorageKey(dummyKey);
        song.setCoverStorageKey(dummyKey);
        song.setCreatedAt(Instant.now());
        return songRepository.save(song);
    }

    private void createReaction(AppUser user, Song song, ReactionType type) {
        SongReaction reaction = new SongReaction();
        reaction.setUser(user);
        reaction.setSong(song);
        reaction.setReactionType(type);
        reaction.setReactedAt(Instant.now());
        songReactionRepository.save(reaction);
    }
}