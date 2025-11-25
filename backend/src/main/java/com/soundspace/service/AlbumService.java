package com.soundspace.service;

import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.CreateAlbumRequest;
import com.soundspace.entity.Album;
import com.soundspace.entity.Song;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.AlbumNotFoundException;
import com.soundspace.repository.AlbumRepository;
import com.soundspace.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final AppUserService appUserService;
    private final SongCoreService songCoreService;
    private final SongRepository songRepository;

    public Optional<Album> findById(Long id) {
        if (id == null) return Optional.empty();
        return albumRepository.findById(id);
    }

    public List<AlbumDto> getUserAlbums(Long userId) {
        return albumRepository.findAllByAuthorId(userId)
                .stream()
                .map(AlbumDto::toDto)
                .toList();
    }

    public AlbumDto createAlbum(CreateAlbumRequest request) {

        if (request.getTitle() == null || request.getTitle().isBlank())
            throw new IllegalArgumentException("Title cannot be null or empty");

        if (request.getDescription() == null || request.getDescription().isBlank())
            request.setDescription(request.getTitle());

        Album album = Album.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(appUserService.getUserById(request.getAuthorId()))
                .publiclyVisible(request.isPubliclyVisible())
                .createdAt(Instant.now())
                .build();
        album = albumRepository.save(album);
        return AlbumDto.toDto(album);
    }

    public AlbumDto getAlbum(Long id) {
        Optional<Album> album = albumRepository.findById(id);
        if (album.isEmpty()) throw new IllegalArgumentException("Nie znaleziono albumu");
        return AlbumDto.toDto(album.get());
    }

    @Transactional
    public SongDto addSongToAlbum(Long albumId, Long songId, String userEmail) {
        Album album = getAlbumById(albumId).orElseThrow(
                () -> new AlbumNotFoundException(albumId));

        // jezeli album jest prywatny i requestujacy user nie jest autorem albumu - throw
        if (!album.getPubliclyVisible() && !appUserService.getUserByEmail(userEmail).getId()
                .equals(album.getAuthor().getId()))
            throw new AccessDeniedException("Ten album jest prywatny. Brak uprawnień");

        Song song = songCoreService.getSongById(songId);

        album.getSongs().add(song);
        song.setAlbum(album);

        return SongDto.toDto(songRepository.save(song));
    }

    public Optional<Album> getAlbumById(Long albumId) {
        return albumRepository.findById(albumId);
    }
}
