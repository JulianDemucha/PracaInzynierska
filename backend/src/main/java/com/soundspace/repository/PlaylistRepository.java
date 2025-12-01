package com.soundspace.repository;
import com.soundspace.entity.Playlist;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlaylistRepository extends Repository<Playlist, Long> {
    Playlist save(Playlist playlist);

    Optional<Playlist> findById(Long playlistId);

    /// bulk delete wszystkich playlist nalezacych do usera - do bulk delete calego usera.
    /// zeby uzyc gdzies indziej trzeba miec na uwadze, ze to nie usuwa storagekeys ani plikow albumow, ani zadnych PlaylistEntry
    @Modifying
    @Query("DELETE FROM Playlist p WHERE p.creator.id = :userId")
    void deleteAllByCreatorId(@Param("userId") Long userId);
}
