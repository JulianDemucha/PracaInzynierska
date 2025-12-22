package com.soundspace.service;

import com.soundspace.dto.SongBaseDto;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.SongReaction;
import com.soundspace.entity.SongStatistics;
import com.soundspace.entity.StorageKey;
import com.soundspace.enums.Genre;
import com.soundspace.enums.ReactionType;
import com.soundspace.enums.Role;
import com.soundspace.enums.Sex;
import com.soundspace.enums.UserAuthProvider;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.repository.SongReactionRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.SongStatisticsRepository;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.service.song.RecommendationsService;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private SongStatisticsRepository songStatisticsRepository;

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
                .email("sigma@sigma.sigma")
                .login("sigma")
                .passwordHash("pass-hash")
                .role(Role.ROLE_USER)
                .sex(Sex.MALE)
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(Instant.now())
                .avatarStorageKey(dummyKey)
                .build();
        appUserRepository.save(testUser);
        recommendationsService.updateViewCap();
    }

    @Test
    void shouldRankGenreMatchedSongsHigherThanBackfill() {
        Song likedRockSong = createSong("Classic Rock Hit", Genre.ROCK, null, 0);
        createReaction(testUser, likedRockSong, ReactionType.LIKE);

        Song rockSong = createSong("Rock Song", Genre.ROCK, null, 0);
        Song popSong = createSong("Pop Song", Genre.POP, null, 0);

        UserDetails userDetails = createUserDetails();

        List<SongBaseDto> recommendations = recommendationsService.getRecommendations(userDetails);

        assertThat(recommendations)
                .extracting(SongBaseDto::title)
                .containsSubsequence("Rock Song", "Pop Song");
    }

    @Test
    void shouldPrioritizeFavouriteGenresOverLikedGenres() {
        Song jazzHistory = createSong("Jazz History", Genre.JAZZ, null, 0);
        createReaction(testUser, jazzHistory, ReactionType.FAVOURITE);

        Song popHistory = createSong("Pop History", Genre.POP, null, 0);
        createReaction(testUser, popHistory, ReactionType.LIKE);

        Song newJazzSong = createSong("New Jazz Standard", Genre.JAZZ, null, 100);
        Song newPopSong = createSong("New Pop Hit", Genre.POP, null, 100);

        UserDetails userDetails = createUserDetails();

        List<SongBaseDto> recommendations = recommendationsService.getRecommendations(userDetails);

        assertThat(recommendations)
                .extracting(SongBaseDto::title)
                .containsSubsequence("New Jazz Standard", "New Pop Hit");
    }

    @Test
    void shouldPrioritizeHighViewCountSongsWithinSameGenre() {
        Song rockHistory = createSong("Rock History", Genre.ROCK, null, 0);
        createReaction(testUser, rockHistory, ReactionType.LIKE);

        Song viralRock = createSong("Viral Rock Anthem", Genre.ROCK, null, 1_000_000);
        Song garageRock = createSong("Garage Demo", Genre.ROCK, null, 10);

        recommendationsService.updateViewCap();

        UserDetails userDetails = User.withUsername(testUser.getEmail()).password("pass").roles("USER").build();

        List<SongBaseDto> recommendations = recommendationsService.getRecommendations(userDetails);

        assertThat(recommendations)
                .extracting(SongBaseDto::title)
                .containsSubsequence("Viral Rock Anthem", "Garage Demo");
    }

    @Test
    void shouldPrioritizeLikedAuthors() {
        AppUser likedArtist = createUser("liked_artist");
        Song artistSong = createSong("Old Hit", Genre.POP, likedArtist, 0);

        createReaction(testUser, artistSong, ReactionType.LIKE);

        AppUser randomArtist = createUser("random_artist");

        Song songByLikedArtist = createSong("New Single by Liked", Genre.POP, likedArtist, 100);
        Song songByRandomArtist = createSong("New Single by Random", Genre.POP, randomArtist, 100);

        UserDetails userDetails = createUserDetails();

        List<SongBaseDto> recommendations = recommendationsService.getRecommendations(userDetails);

        assertThat(recommendations)
                .extracting(SongBaseDto::title)
                .containsSubsequence("New Single by Liked", "New Single by Random");
    }

    @Test
    void shouldReturnEmptyListForColdStart() {
        UserDetails userDetails = createUserDetails();
        List<SongBaseDto> recommendations = recommendationsService.getRecommendations(userDetails);

        assertThat(recommendations).isEmpty();
    }

    // HELPERY

    private UserDetails createUserDetails() {
        return User.withUsername(testUser.getEmail())
                .password("pass")
                .roles("USER")
                .build();
    }

    private Song createSong(String title, Genre genre, AppUser specificAuthor, long views) {
        Song song = new Song();
        song.setTitle(title);
        song.setAuthor(specificAuthor != null ? specificAuthor : testUser);
        song.setPubliclyVisible(true);
        song.setGenres(List.of(genre));
        song.setAudioStorageKey(dummyKey);
        song.setCoverStorageKey(dummyKey);
        song.setCreatedAt(Instant.now());

        song = songRepository.save(song);

        SongStatistics stats = song.getStatistics();
        stats.setViewCount(views);
        songStatisticsRepository.save(stats);

        return song;
    }

    private void createReaction(AppUser user, Song song, ReactionType type) {
        SongReaction reaction = new SongReaction();
        reaction.setUser(user);
        reaction.setSong(song);
        reaction.setReactionType(type);
        reaction.setReactedAt(Instant.now());
        songReactionRepository.save(reaction);
    }

    private AppUser createUser(String login) {
        AppUser user = AppUser.builder()
                .email(login + "@test.com")
                .login(login)
                .passwordHash("pass")
                .role(Role.ROLE_USER)
                .sex(Sex.MALE)
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(Instant.now())
                .avatarStorageKey(dummyKey)
                .build();
        return appUserRepository.save(user);
    }
}