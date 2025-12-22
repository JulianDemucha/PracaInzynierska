package com.soundspace.controller;

import com.soundspace.dto.ProcessedImage;
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
import com.soundspace.service.AlbumService;
import com.soundspace.service.storage.ImageService;
import com.soundspace.service.storage.StorageService;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.media.audio.upload-max-bytes=10485760",
        "app.media.audio.target-extension=m4a",
        "app.media.audio.target-directory=songs/audio",
        "app.media.cover.width=300",
        "app.media.cover.height=300",
        "app.media.cover.quality=0.8",
        "app.media.cover.target-extension=jpg",
        "app.media.cover.song-directory=songs/covers",
        "app.media.cover.default-cover-id=1"
})
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

    @MockitoBean
    private Tika tika;

    @MockitoBean
    private AlbumService albumService;

    private AppUser author;
    private Song publicSong;
    private Song privateSong;

    @BeforeEach
    void setUp() throws Exception {
        given(storageService.saveFromPath(any(), any(), any(), any()))
                .willAnswer(invocation -> "fake-s3-key-" +
                                          java.util.UUID.randomUUID().toString());

        given(tika.detect(any(File.class))).willReturn("audio/mp4");

        ProcessedImage dummyProcessedImage = new ProcessedImage(
                new byte[10],
                "generated-cover.jpg",
                "image/jpeg"
        );

        given(imageService.resizeImageAndConvert(any(), anyInt(),
                anyInt(), any(), anyDouble()))
                .willReturn(dummyProcessedImage);

        StorageKey dummyKey = new StorageKey();
        dummyKey.setKeyStr("avatar-key-" + java.util.UUID.randomUUID());
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
    @WithMockUser(username = "sigma@soundspace.com", roles = "USER")
    void uploadSong_Success() throws Exception {
        MockMultipartFile audioFile = new MockMultipartFile(
                "audioFile",
                "song.m4a",
                "audio/mp4",
                "fake-audio-content".getBytes()
        );

        MockMultipartFile coverFile = new MockMultipartFile(
                "coverFile",
                "cover.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/songs/upload")
                        .file(audioFile)
                        .file(coverFile)
                        .param("title", "Nowy Hit Lata")
                        .param("publiclyVisible", "true")
                        .param("genre", "POP")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nowy Hit Lata"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void getSongById_Public_Anonymous() throws Exception {
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
    void getSongByIdPrivateOtherUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/songs/{id}", privateSong.getId()))
                .andExpect(status().isForbidden());
    }
}