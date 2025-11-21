package com.soundspace.service;

import com.soundspace.entity.Album;
import com.soundspace.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;

    public Album findById(Long id) {
        if(id == null) return null;
        return albumRepository.findById(id).orElse(null);
    }
}
