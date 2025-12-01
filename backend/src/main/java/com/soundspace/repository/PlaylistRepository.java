package com.soundspace.repository;
import com.soundspace.entity.Playlist;
import org.springframework.data.repository.Repository;
import java.util.Optional;

public interface PlaylistRepository extends Repository<Playlist, Long> {
    Playlist save(Playlist playlist);

    Optional<Playlist> findById(Long playlistId);
}
