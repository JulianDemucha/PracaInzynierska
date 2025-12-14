package com.soundspace.repository;
import com.soundspace.dto.projection.PlaylistProjection;
import com.soundspace.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            SELECT DISTINCT p
            FROM Playlist p
            LEFT JOIN FETCH p.creator
            LEFT JOIN FETCH p.coverStorageKey
            WHERE p.creator.id = :userId
            AND p.publiclyVisible = true
            """)
    List<Playlist> getAllPublicByCreatorId(@Param("userId") Long userId);

    @Query(value = """
            SELECT
            p.id AS id,
            p.title AS title,
            u.id AS creatorId,
            u.login AS creatorUsername,
            p.publiclyVisible AS publiclyVisible,
            p.createdAt AS createdAt,
            p.updatedAt AS updatedAt,
            sk.id AS coverStorageKeyId,
            CAST(COUNT(pe) AS int) AS songsCount
            FROM Playlist p
            LEFT JOIN p.creator u
            LEFT JOIN p.coverStorageKey sk
            LEFT JOIN p.songs pe
            GROUP BY p.id, u.id, sk.id
            """)
    List<PlaylistProjection> findAllWithDetails();

    @Query(value = """
        SELECT
        p.id AS id,
        p.title AS title,
        u.id AS creatorId,
        u.login AS creatorUsername,
        p.publiclyVisible AS publiclyVisible,
        p.createdAt AS createdAt,
        p.updatedAt AS updatedAt,
        sk.id AS coverStorageKeyId,
        CAST(COUNT(pe) AS int) AS songsCount
        FROM Playlist p
        LEFT JOIN p.creator u
        LEFT JOIN p.coverStorageKey sk
        LEFT JOIN p.songs pe
        WHERE p.publiclyVisible = true OR u.id = :userId
        GROUP BY p.id, u.id, sk.id
        """)
    List<PlaylistProjection> findAllPublicOrOwnedByUser(@Param("userId") Long userId);

    @Query(value = """
        SELECT
        p.id AS id,
        p.title AS title,
        u.id AS creatorId,
        u.login AS creatorUsername,
        p.publiclyVisible AS publiclyVisible,
        p.createdAt AS createdAt,
        p.updatedAt AS updatedAt,
        sk.id AS coverStorageKeyId,
        CAST(COUNT(pe) AS int) AS songsCount
        FROM Playlist p
        LEFT JOIN p.creator u
        LEFT JOIN p.coverStorageKey sk
        LEFT JOIN p.songs pe
        WHERE p.publiclyVisible = true
        GROUP BY p.id, u.id, sk.id
        """)
    List<PlaylistProjection> findAllPublic();

    @Query(value = """
            SELECT p.id,
                   p.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   p.publicly_visible,
                   p.created_at,
                   sk.id AS cover_storage_key_id,

                   (CASE WHEN LOWER(p.title) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                    CASE WHEN LOWER(p.title) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                    CASE WHEN LOWER(p.title) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END)
                                   AS relevance_score

            FROM playlists p
            LEFT JOIN app_users u ON u.id = p.user_id
            LEFT JOIN storage_keys sk ON sk.id = p.cover_storage_key_id
            
            WHERE (p.title ILIKE :containsQuery)
              AND (p.publicly_visible = true OR p.user_id = :userId)
            
            ORDER BY relevance_score DESC, p.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT p.id)
            FROM playlists p
            WHERE (p.title ILIKE :containsQuery)
              AND (p.publicly_visible = true OR p.user_id = :userId)
            """,
            nativeQuery = true)
    Page<PlaylistProjection> searchPlaylists(
            @Param("exactQuery") String exactQuery,
            @Param("startsWithQuery") String startsWithQuery,
            @Param("containsQuery") String containsQuery,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(value = """
            SELECT p.id,
                   p.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   p.publicly_visible,
                   p.created_at,
                   sk.id AS cover_storage_key_id,

                   (CASE WHEN LOWER(p.title) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                    CASE WHEN LOWER(p.title) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                    CASE WHEN LOWER(p.title) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END)
                                   AS relevance_score

            FROM playlists p
            LEFT JOIN app_users u ON u.id = p.user_id
            LEFT JOIN storage_keys sk ON sk.id = p.cover_storage_key_id
            
            WHERE (p.title ILIKE :containsQuery)
              AND p.publicly_visible = true
            
            ORDER BY relevance_score DESC, p.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT a.id)
            FROM albums a
            WHERE (a.title ILIKE :containsQuery)
              AND a.publicly_visible = true
            """,
            nativeQuery = true)
    Page<PlaylistProjection> searchPlaylistsPublic(
            @Param("exactQuery") String exactQuery,
            @Param("startsWithQuery") String startsWithQuery,
            @Param("containsQuery") String containsQuery,
            Pageable pageable
    );
}
