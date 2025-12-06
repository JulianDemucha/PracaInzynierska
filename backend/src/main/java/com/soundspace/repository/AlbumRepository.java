package com.soundspace.repository;

import com.soundspace.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.soundspace.enums.Genre;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Album getAlbumById(Long id);

    @Query("""
            SELECT DISTINCT a
            FROM Album a
            LEFT JOIN FETCH a.author
            LEFT JOIN FETCH a.coverStorageKey
            LEFT JOIN FETCH a.songs
            JOIN a.genres g
            WHERE a.author.id = :authorId
            """)
    List<Album> findAllByAuthorId(@Param("authorId") Long authorId);

    @Query("""
            SELECT DISTINCT a
            FROM Album a
            LEFT JOIN FETCH a.author
            LEFT JOIN FETCH a.coverStorageKey
            LEFT JOIN FETCH a.songs
            JOIN a.genres g
            WHERE a.author.id = :authorId
            AND a.publiclyVisible = true
            """)
    List<Album> findPublicByAuthorId(@Param("authorId") Long authorId);

    @Query("""
            SELECT DISTINCT a
            FROM Album a
            LEFT JOIN FETCH a.author
            LEFT JOIN FETCH a.coverStorageKey
            LEFT JOIN FETCH a.songs
            JOIN a.genres g
            WHERE g = :genre
            AND a.publiclyVisible = true
            """)
    List<Album> findPublicByGenre(@Param("genre") Genre genre);

    @Query("""
            SELECT DISTINCT a FROM Album a
            LEFT JOIN FETCH a.author
            LEFT JOIN FETCH a.coverStorageKey
            WHERE a.publiclyVisible = true
            """)
    List<Album> findAllPublic();

    @Query("""
            SELECT DISTINCT a
            FROM Album a
            LEFT JOIN FETCH a.author
            LEFT JOIN FETCH a.coverStorageKey
            LEFT JOIN FETCH a.songs
            JOIN a.genres g
            WHERE g = :genre
            AND a.author.email = :userEmail
            """)
    List<Album> findPublicOrOwnedByUserByGenre(@Param("genre") Genre genre, @Param("userEmail") String userEmail);

    @Query("""
            SELECT DISTINCT a FROM Album a
            LEFT JOIN FETCH a.author
            LEFT JOIN FETCH a.coverStorageKey
            WHERE a.author.email = :userEmail
            OR a.publiclyVisible = true
            """)
    List<Album> findAllPublicOrOwnedByUser(@Param("userEmail") String userEmail);

    @Modifying
    @Query( value = """
            UPDATE songs
            SET cover_storage_key_id = (SELECT cover_storage_key_id FROM albums WHERE id = :albumId)
            WHERE album_id = :albumId
            """, nativeQuery = true)
    void refreshCoverStorageKeyInAlbumSongs(@Param("albumId") Long albumId);

    @Modifying
    @Query( value = """
            UPDATE songs
            SET publicly_visible = (SELECT albums.publicly_visible FROM albums WHERE id = :albumId)
            WHERE album_id = :albumId
            """, nativeQuery = true)
    void refreshPubliclyVisibleInAlbumSongs(@Param("albumId") Long albumId);


    /// bulk delete wszystkich albumow nalezacych do usera - do bulk delete calego usera.
    /// zeby uzyc gdzies indziej trzeba miec na uwadze, ze to nie usuwa storagekeys ani plikow albumow, ani piosenek,
    /// ktore na te albumy wskazuja
    @Modifying
    @Query("DELETE FROM Album a WHERE a.author.id = :userId")
    void deleteAllByAuthorId(@Param("userId") Long userId);
}
