package com.soundspace.controller;

import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.StorageKey;
import com.soundspace.enums.Role;
import com.soundspace.enums.Sex;
import com.soundspace.enums.UserAuthProvider;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppUserDeletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private StorageKeyRepository storageKeyRepository;

    @MockitoBean
    private StorageService storageService;

    private AppUser victimUser;
    private AppUser adminUser;

    @BeforeEach
    void setUp() {
        StorageKey dummyKey = new StorageKey();
        dummyKey.setKeyStr("key-" + UUID.randomUUID());
        dummyKey.setMimeType("image/jpeg");
        dummyKey.setSizeBytes(100L);
        dummyKey.setCreatedAt(Instant.now());
        storageKeyRepository.save(dummyKey);

        victimUser = AppUser.builder()
                .email("victim@soundspace.com")
                .login("victim")
                .passwordHash("pass")
                .role(Role.ROLE_USER)
                .sex(Sex.MALE)
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(Instant.now())
                .avatarStorageKey(dummyKey)
                .build();
        appUserRepository.save(victimUser);

        adminUser = AppUser.builder()
                .email("admin@soundspace.com")
                .login("admin")
                .passwordHash("pass")
                .role(Role.ROLE_ADMIN)
                .sex(Sex.FEMALE)
                .authProvider(UserAuthProvider.LOCAL)
                .emailVerified(true)
                .createdAt(Instant.now())
                .avatarStorageKey(dummyKey)
                .build();
        appUserRepository.save(adminUser);

        Song song = new Song();
        song.setTitle("Sad Song");
        song.setAuthor(victimUser);
        song.setPubliclyVisible(true);
        song.setCreatedAt(Instant.now());
        song.setAudioStorageKey(dummyKey);
        song.setCoverStorageKey(dummyKey);
        songRepository.save(song);

        songRepository.flush();
        appUserRepository.flush();
    }

    @Test
    @WithMockUser(username = "victim@soundspace.com", roles = "USER")
    void shouldDeleteSelfAndCascadeData() throws Exception {
        assertTrue(appUserRepository.findByEmail("victim@soundspace.com").isPresent());
        assertTrue(songRepository.existsByAuthorId(victimUser.getId()));

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isNoContent());

        assertTrue(appUserRepository.findByEmail("victim@soundspace.com").isEmpty());

        assertTrue(songRepository.findByAuthorId(victimUser.getId()).isEmpty());

        verify(storageService).deleteAllUserFiles(anyLong());
    }

    @Test
    @WithMockUser(username = "admin@soundspace.com", roles = "ADMIN")
    void adminShouldDeleteOtherUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", victimUser.getId()))
                .andExpect(status().isNoContent()); // 204

        assertTrue(appUserRepository.findByEmail("victim@soundspace.com").isEmpty());
        verify(storageService).deleteAllUserFiles(anyLong());
    }

    @Test
    @WithMockUser(username = "other@soundspace.com", roles = "USER")
    void userCannotDeleteOtherUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", victimUser.getId()))
                .andExpect(status().isForbidden()); // 403

        assertTrue(appUserRepository.findByEmail("victim@soundspace.com").isPresent());
    }

    @Test
    @WithMockUser(username = "admin@soundspace.com", roles = "ADMIN")
    void adminCannotDeleteSelfViaAdminEndpoint() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", adminUser.getId()))
                .andExpect(status().isBadRequest());

        assertTrue(appUserRepository.findByEmail("admin@soundspace.com").isPresent());
    }
}