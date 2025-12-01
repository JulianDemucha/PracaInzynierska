package com.soundspace.repository;

import com.soundspace.dto.projection.SongProjection;
import com.soundspace.entity.Song;
import com.soundspace.enums.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                    u.login AS author_username,
                   s.album_id,
                   string_agg(g.genre, ',') AS genresStr,
                   s.publicly_visible,
                   s.created_at,
                   sk.id AS cover_storage_key_id
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN song_genres g ON g.song_id = s.id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            WHERE s.user_id = :userId
            GROUP BY s.id, u.id, s.album_id, s.publicly_visible, s.created_at, sk.id
            """, nativeQuery = true)
    List<SongProjection> findSongsByUserNative(@Param("userId") Long userId);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   s.album_id,
                   string_agg(g.genre, ',') AS genresStr,
                   s.publicly_visible,
                   s.created_at,
                   sk.id AS cover_storage_key_id
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN song_genres g ON g.song_id = s.id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            WHERE s.album_id = :albumId
            GROUP BY s.id, u.id, s.album_id, s.publicly_visible, s.created_at, sk.id
            """, nativeQuery = true)
    List<SongProjection> findSongsByAlbumNative(@Param("albumId") Long albumId);


    @Query(value = """
            SELECT DISTINCT s FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN FETCH s.audioStorageKey
            LEFT JOIN FETCH s.album
            WHERE s.publiclyVisible = true
            """)
    List<Song> findAllWithDetails();

    @Query("SELECT s FROM Song s JOIN s.genres g WHERE g = :genre")
    List<Song> findAllByGenre(@Param("genre") Genre genre);

    /// bulk delete wszystkich songow nalezacych do usera - do bulk delete calego usera.
    /// zeby uzyc gdzies indziej trzeba miec na uwadze, ze to nie usuwa storagekeys ani plikow piosenek
    @Modifying
    @Query("DELETE FROM Song s WHERE s.author.id = :userId")
    void deleteAllByAuthorId(@Param("userId") Long userId);
}
