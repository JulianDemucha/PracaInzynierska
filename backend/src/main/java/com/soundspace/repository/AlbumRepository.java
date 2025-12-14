package com.soundspace.repository;

import com.soundspace.dto.projection.AlbumProjection;
import com.soundspace.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
            SELECT a.id,
                   a.title,
                   a.description,
                   u.id AS author_id,
                   u.login AS author_username,
                   a.publicly_visible,
                   a.created_at,
                   sk.id AS cover_storage_key_id,
                   COALESCE(g.genresStr, '') AS genresStr,

                   (CASE WHEN LOWER(a.title) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                    CASE WHEN LOWER(a.title) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                    CASE WHEN LOWER(a.title) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END)
                                   AS relevance_score

            FROM albums a
            LEFT JOIN app_users u ON u.id = a.user_id
            LEFT JOIN storage_keys sk ON sk.id = a.cover_storage_key_id
                        LEFT JOIN (
                SELECT album_id, string_agg(DISTINCT genre, ',') AS genresStr
                FROM album_genres g
                GROUP BY album_id
            ) g ON g.album_id = a.id
            
            WHERE (a.title ILIKE :containsQuery)
              AND (a.publicly_visible = true OR a.user_id = :userId)
            
            ORDER BY relevance_score DESC, a.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT a.id)
            FROM albums a
            WHERE (a.title ILIKE :containsQuery)
              AND (a.publicly_visible = true OR a.user_id = :userId)
            """,
            nativeQuery = true)
    Page<AlbumProjection> searchAlbums(
            @Param("exactQuery") String exactQuery,
            @Param("startsWithQuery") String startsWithQuery,
            @Param("containsQuery") String containsQuery,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(value = """
            SELECT a.id,
                   a.title,
                   a.description,
                   u.id AS author_id,
                   u.login AS author_username,
                   a.publicly_visible,
                   a.created_at,
                   sk.id AS cover_storage_key_id,
                   COALESCE(g.genresStr, '') AS genresStr,

                   (CASE WHEN LOWER(a.title) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                    CASE WHEN LOWER(a.title) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                    CASE WHEN LOWER(a.title) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END)
                                   AS relevance_score

            FROM albums a
            LEFT JOIN app_users u ON u.id = a.user_id
            LEFT JOIN storage_keys sk ON sk.id = a.cover_storage_key_id
                        LEFT JOIN (
                SELECT album_id, string_agg(DISTINCT genre, ',') AS genresStr
                FROM album_genres g
                GROUP BY album_id
            ) g ON g.album_id = a.id
            
            WHERE (a.title ILIKE :containsQuery)
              AND a.publicly_visible = true
            
            ORDER BY relevance_score DESC, a.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT a.id)
            FROM albums a
            WHERE (a.title ILIKE :containsQuery)
              AND a.publicly_visible = true
            """,
            nativeQuery = true)
    Page<AlbumProjection> searchAlbumsPublic(
            @Param("exactQuery") String exactQuery,
            @Param("startsWithQuery") String startsWithQuery,
            @Param("containsQuery") String containsQuery,
            Pageable pageable
    );
}
