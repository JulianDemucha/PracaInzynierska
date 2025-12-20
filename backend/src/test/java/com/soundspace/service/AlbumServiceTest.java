package com.soundspace.service;

import com.soundspace.dto.AlbumDto;
import com.soundspace.entity.Album;
import com.soundspace.entity.AppUser;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.AlbumNotFoundException;
import com.soundspace.repository.AlbumRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void getAlbumByIdShouldReturnAlbumDto() {
        Long albumId = 1L;
        Album album = new Album();
        album.setId(albumId);
        album.setTitle("sigmastyczny album");
        album.setPubliclyVisible(true);
        album.setAuthor(new AppUser());
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        AlbumDto result = albumService.getAlbum(albumId, null);

        assertNotNull(result);
        assertEquals(albumId, result.id());
        assertEquals("sigmastyczny album", result.title());
    }

    @Test
    void getAlbumByIdShouldThrowWhenAlbumDoesNotExist() {
        Long albumId = 1L;
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        assertThrows(AlbumNotFoundException.class, () -> {
            albumService.getAlbum(albumId, null);
        });
    }

    @Test
    void getAlbumByIdShouldThrowWhenAlbumIsPrivateAndUserNotLoggedIn() {
        Long albumId = 1L;
        Album album = new Album();
        album.setId(albumId);
        album.setPubliclyVisible(false);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));

        assertThrows(AccessDeniedException.class, () -> {
            albumService.getAlbum(albumId, null);
        });
    }
}