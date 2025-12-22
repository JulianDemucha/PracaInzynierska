package com.soundspace.repository;

import com.soundspace.dto.projection.RecommendationsSongProjection;
import com.soundspace.dto.projection.SongBaseProjection;
import com.soundspace.entity.Song;
import com.soundspace.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

// todo pozmieniac niepotrzebne left joiny w inner joiny kiedy relacja i tak jest obowiazkowa
public interface SongRepository extends JpaRepository<Song, Long> {

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            WHERE s.user_id = :userId
            """, nativeQuery = true)
    List<SongBaseProjection> findSongsByUserNative(@Param("userId") Long userId);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            WHERE s.user_id = :userId
            AND s.publicly_visible = true
            """, nativeQuery = true)
    List<SongBaseProjection> findPublicSongsByUserNative(@Param("userId") Long userId);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            WHERE s.album_id = :albumId
            """, nativeQuery = true)
    List<SongBaseProjection> findSongsByAlbumNative(@Param("albumId") Long albumId);


    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            WHERE s.publicly_visible = true
            """,
            countQuery = """
            SELECT COUNT(*) FROM songs s
            WHERE s.publicly_visible = true
            """,
            nativeQuery = true)
    Page<SongBaseProjection> findAllPublic(Pageable pageable);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            WHERE s.user_id = :userId
               OR s.publicly_visible = true
            """,
            countQuery = """
            SELECT COUNT(*) FROM songs s
            WHERE s.user_id = :userId
               OR s.publicly_visible = true
            """,
            nativeQuery = true)
    Page<SongBaseProjection> findAllPublicOrOwnedByUser(@Param("userId") Long userId, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            JOIN song_genres g ON g.song_id = s.id
            WHERE g.genre = :#{#genre.name()}
              AND (s.user_id = :userId OR s.publicly_visible = true)
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s.id)
            FROM songs s
            JOIN song_genres g ON g.song_id = s.id
            WHERE g.genre = :#{#genre.name()}
              AND (s.user_id = :userId OR s.publicly_visible = true)
            """,
            nativeQuery = true)
    Page<SongBaseProjection> findPublicOrOwnedByUserByGenre(@Param("genre") Genre genre,
                                                            @Param("userId") Long userId,
                                                            Pageable pageable);

    @Query(value = """
            SELECT DISTINCT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            JOIN song_genres g ON g.song_id = s.id
            WHERE g.genre = :#{#genre.name()}
              AND s.publicly_visible = true
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s.id)
            FROM songs s
            JOIN song_genres g ON g.song_id = s.id
            WHERE g.genre = :#{#genre.name()}
              AND s.publicly_visible = true
            """,
            nativeQuery = true)
    Page<SongBaseProjection> findPublicByGenre(
            @Param("genre") Genre genre,
            Pageable pageable
    );

    /// bulk delete wszystkich songow nalezacych do usera - do bulk delete calego usera.
    /// zeby uzyc gdzies indziej trzeba miec na uwadze, ze to nie usuwa storagekeys ani plikow piosenek
    @Modifying
    @Query("DELETE FROM Song s WHERE s.author.id = :userId")
    void deleteAllByAuthorId(@Param("userId") Long userId);

    @Query("""
            SELECT s.id AS id, s.genres AS genres, s.author.id AS authorId
            FROM Song s
            JOIN s.genres g
            JOIN SongReaction r ON r.song = s
            WHERE r.user.id = :userId
              AND r.reactionType = 'LIKE'
            """)
    List<RecommendationsSongProjection> findAllLikedByAppUserIdForRecommendations(@Param("userId") Long userId);

    @Query("""
            SELECT s.id AS id, s.genres AS genres, s.author.id AS authorId
            FROM Song s
            JOIN s.genres g
            JOIN SongReaction r ON r.song = s
            WHERE r.user.id = :userId
              AND r.reactionType = 'DISLIKE'
            """)
    List<RecommendationsSongProjection> findAllDislikedByAppUserIdForRecommendations(@Param("userId") Long userId);

    @Query("""
            SELECT s.id AS id, s.genres AS genres, s.author.id AS authorId
            FROM Song s
            JOIN s.genres g
            JOIN SongReaction r ON r.song = s
            WHERE r.user.id = :userId
              AND r.reactionType = 'FAVOURITE'
            """)
    List<RecommendationsSongProjection> findAllFavouriteByAppUserIdForRecommendations(@Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT s
            FROM SongReaction r
            JOIN r.song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            WHERE r.user.id = :userId
              AND r.reactionType = 'FAVOURITE'
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s)
            FROM SongReaction r
            JOIN r.song s
            WHERE r.user.id = :userId
            AND r.reactionType = 'FAVOURITE'
            """)
    Page<Song> findAllFavouriteByAppUserId(@Param("userId") Long userId, Pageable pageable);

    /// metoda zwraca piosenki:
    /// - posiadajace w sobie przynajmniej jeden genre z podanych
    /// - nalezace do podanych autorow
    ///
    /// zwracane piosenki to 'kandydaci' do wytworzenia listy rekomendowanych utworów.
    @Query("""
            SELECT DISTINCT s
            FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.statistics
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN s.genres g
            
            WHERE (g IN :genres OR s.author.id IN :authorIds)
              AND s.publiclyVisible = true
              AND s.id NOT IN (
                  SELECT r.song.id FROM SongReaction r
                  WHERE r.user.id = :userId
              )
            ORDER BY s.createdAt DESC
            """)
    List<Song> findCandidates(@Param("genres") Collection<Genre> genres,
                              @Param("authorIds") Collection<Long> authorIds,
                              @Param("userId") Long userId,
                              Pageable pageable);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            LEFT JOIN (
              SELECT song_id, COUNT(*) AS views
              FROM song_views
              WHERE viewed_at > :cutoffDate
              GROUP BY song_id
            ) vv ON vv.song_id = s.id
            
            LEFT JOIN (
              SELECT song_id, COUNT(*) AS likes
              FROM song_reactions
              WHERE reaction_type = 'LIKE' AND reacted_at > :cutoffDate
              GROUP BY song_id
            ) rr ON rr.song_id = s.id
            
            WHERE s.publicly_visible = true
            AND (COALESCE(vv.views, 0) + COALESCE(rr.likes, 0)) > 0
            
            ORDER BY (COALESCE(vv.views, 0) + COALESCE(rr.likes, 0) * :likeWage) DESC, s.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM (
              SELECT s.id
              FROM songs s
              LEFT JOIN (
                SELECT song_id, COUNT(*) AS views
                FROM song_views
                WHERE viewed_at > :cutoffDate
                GROUP BY song_id
              ) vv ON vv.song_id = s.id
              LEFT JOIN (
                SELECT song_id, COUNT(*) AS likes
                FROM song_reactions
                WHERE reaction_type = 'LIKE' AND reacted_at > :cutoffDate
                GROUP BY song_id
              ) rr ON rr.song_id = s.id
              WHERE s.publicly_visible = true
              AND (COALESCE(vv.views, 0) + COALESCE(rr.likes, 0)) > 0
            ) t
            """,
            nativeQuery = true)
    Page<SongBaseProjection> findTrendingSongs(
            @Param("cutoffDate") Instant cutoffDate,
            Pageable pageable,
            @Param("likeWage") int likeWage
    );

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            LEFT JOIN song_statistics ss ON ss.song_id = s.id
            
            WHERE s.publicly_visible = true
            ORDER BY COALESCE(ss.likes_count, 0) DESC, COALESCE(ss.view_count, 0) DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM songs s
            WHERE s.publicly_visible = true
            """,
            nativeQuery = true)
    Page<SongBaseProjection> findTopLikedSongs(Pageable pageable);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at
            
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            LEFT JOIN song_statistics ss ON ss.song_id = s.id
            WHERE s.publicly_visible = true
            
            ORDER BY COALESCE(ss.view_count, 0) DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM songs s
            WHERE s.publicly_visible = true
            """,
            nativeQuery = true)
    Page<SongBaseProjection> findTopViewedSongs(Pageable pageable);

    @Query("""
        SELECT s
        FROM Song s
        LEFT JOIN FETCH s.author
        LEFT JOIN FETCH s.statistics
        LEFT JOIN FETCH s.coverStorageKey
        WHERE s.publiclyVisible = true
        ORDER BY s.statistics.viewCount DESC
        """) //batchsize na genres dociagnie wszystkie w razie czego
    List<Song> findTopPopularSongs(Pageable pageable);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at,
                   (
                       CASE WHEN LOWER(s.title) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                       CASE WHEN LOWER(s.title) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                       CASE WHEN LOWER(s.title) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END
                   ) AS relevance_score
            
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            LEFT JOIN song_statistics ss ON ss.song_id = s.id
            
            WHERE (s.title ILIKE :containsQuery)
              AND (s.publicly_visible = true OR s.user_id = :userId)
            
            ORDER BY relevance_score DESC, COALESCE(ss.view_count, 0) DESC
            """,
            countQuery = """
            SELECT COUNT(s.id)
            FROM songs s
            WHERE (s.title ILIKE :containsQuery)
              AND (s.publicly_visible = true OR s.user_id = :userId)
            """,
            nativeQuery = true)
    Page<SongBaseProjection> searchSongs(
            @Param("exactQuery") String exactQuery,
            @Param("startsWithQuery") String startsWithQuery,
            @Param("containsQuery") String containsQuery,
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   sk.id AS cover_storage_key_id,
                   CAST(s.created_at AS TEXT) AS created_at,
                   (
                       CASE WHEN LOWER(s.title) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                       CASE WHEN LOWER(s.title) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                       CASE WHEN LOWER(s.title) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END
                   ) AS relevance_score

            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            LEFT JOIN song_statistics ss ON ss.song_id = s.id
            
            WHERE (s.title ILIKE :containsQuery)
              AND s.publicly_visible = true
            
            ORDER BY relevance_score DESC, COALESCE(ss.view_count, 0) DESC
            """,
            countQuery = """
            SELECT COUNT(s.id)
            FROM songs s
            WHERE (s.title ILIKE :containsQuery)
              AND s.publicly_visible = true
            """,
            nativeQuery = true)
    Page<SongBaseProjection> searchSongsPublic(
            @Param("exactQuery") String exactQuery,
            @Param("startsWithQuery") String startsWithQuery,
            @Param("containsQuery") String containsQuery,
            Pageable pageable
    );

    boolean existsByAuthorId(Long authorId);

    List<Song> findByAuthorId(Long authorId);

}

