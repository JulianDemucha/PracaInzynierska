package com.soundspace.service;

import com.soundspace.dto.PlaylistDto;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Playlist;
import com.soundspace.entity.StorageKey;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.repository.PlaylistRepository;
import com.soundspace.service.user.AppUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private AppUserService appUserService;

    @InjectMocks
    private PlaylistService playlistService;

    @Test
    void getPlaylistByIdShouldReturnPlaylistDto() {
        Long playlistId = 1L;

        AppUser creator = new AppUser();
        creator.setLogin("testUser");

        StorageKey coverKey = new StorageKey();
        coverKey.setId(100L);

        Playlist playlist = new Playlist();
        playlist.setId(playlistId);
        playlist.setTitle("Moja playlista");
        playlist.setPubliclyVisible(true);
        playlist.setCreator(creator);
        playlist.setCoverStorageKey(coverKey);

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

        PlaylistDto result = playlistService.getById(playlistId, null);

        assertNotNull(result);
        assertEquals(playlistId, result.id());
        assertEquals("Moja playlista", result.title());
    }

    @Test
    void getPlaylistByIdShouldThrowWhenPlaylistDoesNotExist() {
        Long playlistId = 99L;
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            playlistService.getById(playlistId, null);
        });
    }

    @Test
    void getPlaylistByIdShouldThrowWhenPlaylistIsPrivateAndUserNotLoggedIn() {
        Long playlistId = 1L;

        AppUser creator = new AppUser();
        creator.setId(10L);
        creator.setLogin("creatorUser");

        Playlist playlist = new Playlist();
        playlist.setId(playlistId);
        playlist.setPubliclyVisible(false);
        playlist.setCreator(creator);
        playlist.setCoverStorageKey(new StorageKey());

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

        assertThrows(AccessDeniedException.class, () -> {
            playlistService.getById(playlistId, null);
        });
    }
}