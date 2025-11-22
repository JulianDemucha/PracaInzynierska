package com.soundspace.service;

import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.request.CreateAlbumRequest;
import com.soundspace.entity.Album;
import com.soundspace.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final AppUserService appUserService;

    public Optional<Album> findById(Long id) {
        if (id == null) return Optional.empty();
        return albumRepository.findById(id);
    }

    public AlbumDto createAlbum(CreateAlbumRequest request) {

        if(request.getTitle() == null || request.getTitle().isBlank())
            throw new IllegalArgumentException("Title cannot be null or empty");

        if(request.getDescription() == null || request.getDescription().isBlank())
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
        if(album.isEmpty()) throw new IllegalArgumentException("Album not found");
        return AlbumDto.toDto(album.get());
    }
}
