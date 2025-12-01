package com.soundspace.repository;

import com.soundspace.entity.AppUser;
import com.soundspace.entity.Playlist;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface PlaylistRepository extends Repository<Playlist, Long> {
    List<Playlist> findAllByOrderByIdAsc();
    Playlist findById(long id);

    Playlist save(Playlist playlist);
}
