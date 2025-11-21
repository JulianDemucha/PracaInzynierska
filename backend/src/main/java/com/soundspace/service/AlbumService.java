package com.soundspace.service;

import com.soundspace.entity.Album;
import com.soundspace.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;

    public Optional<Album> findById(Long id) {
        if (id == null) return Optional.empty();
        return albumRepository.findById(id);
    }
}
