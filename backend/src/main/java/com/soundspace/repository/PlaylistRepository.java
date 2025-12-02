package com.soundspace.repository;

import com.soundspace.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /// bulk delete wszystkich playlist nalezacych do usera - do bulk delete calego usera.
    /// zeby uzyc gdzies indziej trzeba miec na uwadze, ze to nie usuwa storagekeys ani plikow albumow, ani zadnych PlaylistEntry
    @Modifying
    @Query("DELETE FROM Playlist p WHERE p.creator.id = :userId")
    void deleteAllByCreatorId(@Param("userId") Long userId);

    @Query("""
            SELECT DISTINCT p
            FROM Playlist p
            LEFT JOIN FETCH p.creator
            LEFT JOIN FETCH p.coverStorageKey
            WHERE p.creator.id = :userId
            """)
    List<Playlist> getAllByCreatorId(@Param("userId") Long userId);
}
