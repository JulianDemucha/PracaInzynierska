package com.soundspace.repository;

import com.soundspace.entity.StorageKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface StorageKeyRepository extends JpaRepository<StorageKey, Long> {
    @Modifying
    @Query("""
        DELETE FROM StorageKey sk
        WHERE sk.key LIKE CONCAT('users/avatars/', :userId, '/%')
           OR sk.key LIKE CONCAT('albums/covers/', :userId, '/%')
           OR sk.key LIKE CONCAT('playlists/covers/', :userId, '/%')
           OR sk.key LIKE CONCAT('songs/covers/', :userId, '/%')
           OR sk.key LIKE CONCAT('songs/audio/', :userId, '/%')
    """)
    void deleteAllByUserId(@Param("userId") Long userId);
}
