package com.soundspace.controller;

import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.StorageKey;
import com.soundspace.enums.Genre;
import com.soundspace.enums.Role;
import com.soundspace.enums.Sex;
import com.soundspace.enums.UserAuthProvider;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.service.storage.ImageService;
import com.soundspace.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SongCoreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private StorageKeyRepository storageKeyRepository;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private ImageService imageService;

    private AppUser author;
    private Song publicSong;
    private Song privateSong;

    @BeforeEach
    void setUp() {
        StorageKey dummyKey = new StorageKey();
        dummyKey.setKeyStr("test-key-" + System.nanoTime());
        dummyKey.setMimeType("image/jpeg");
        dummyKey.setSizeBytes(1024L);
        dummyKey.setCreatedAt(Instant.now());
        storageKeyRepository.save(dummyKey);

        author = appUserRepository.save(AppUser.builder()
                .email("sigma@soundspace.com")
                .login("sigma")
                .passwordHash("hashed_password")
                .role(Role.ROLE_USER)
                .sex(Sex.MALE)
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(Instant.now())
                .avatarStorageKey(dummyKey)
                .build());

        appUserRepository.save(AppUser.builder()
                .email("innasigma@soundspace.com")
                .login("innasigma")
                .passwordHash("hashed_password")
                .role(Role.ROLE_USER)
                .sex(Sex.FEMALE)
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(Instant.now())
                .avatarStorageKey(dummyKey)
                .build());

        publicSong = songRepository.save(Song.builder()
                .title("sigma piosenka roku z dodatkiem rizzu")
                .author(author)
                .publiclyVisible(true)
                .genres(List.of(Genre.POP))
                .audioStorageKey(dummyKey)
                .coverStorageKey(dummyKey)
                .createdAt(Instant.now())
                .build());

        privateSong = songRepository.save(Song.builder()
                .title("pv")
                .author(author)
                .publiclyVisible(false)
                .genres(List.of(Genre.ROCK))
                .audioStorageKey(dummyKey)
                .coverStorageKey(dummyKey)
                .createdAt(Instant.now())
                .build());

        songRepository.flush();
    }

    @Test
    void getSongById_Public_Anonymous() throws Exception {
        mockMvc.perform(get("/api/songs/{id}", publicSong.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(publicSong.getId()))
                .andExpect(jsonPath("$.title").value("sigma piosenka roku z dodatkiem rizzu"))
                .andExpect(jsonPath("$.publiclyVisible").value(true));
    }

    @Test
    @WithMockUser(username = "innasigma@soundspace.com")
    void getSongById_Public_Authenticated() throws Exception {
        mockMvc.perform(get("/api/songs/{id}", publicSong.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("sigma piosenka roku z dodatkiem rizzu"));
    }

    @Test
    @WithMockUser(username = "sigma@soundspace.com")
    void getSongById_PrivateOwner() throws Exception {
        mockMvc.perform(get("/api/songs/{id}", privateSong.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("pv"));
    }

    @Test
    @WithMockUser(username = "innasigma@soundspace.com")
    void getSongByIdPrivateOtherUser() throws Exception {
        mockMvc.perform(get("/api/songs/{id}", privateSong.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSongByIdPrivateAnonymous() throws Exception {
        mockMvc.perform(get("/api/songs/{id}", privateSong.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "sigma@soundspace.com")
    void getSongById_NotFound() throws Exception {
        Long nonExistentId = 999999L;
        mockMvc.perform(get("/api/songs/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}