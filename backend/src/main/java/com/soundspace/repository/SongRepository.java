package com.soundspace.repository;

import com.soundspace.dto.projection.RecommendationsSongProjection;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.entity.Song;
import com.soundspace.enums.Genre;
import com.soundspace.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// todo pozmieniac niepotrzebne left joiny w inner joiny kiedy relacja i tak jest obowiazkowa
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
            WHERE s.user_id = :userId
            AND s.publicly_visible = true
            GROUP BY s.id, u.id, s.album_id, s.publicly_visible, s.created_at, sk.id
            """, nativeQuery = true)
    List<SongProjection> findPublicSongsByUserNative(@Param("userId") Long userId);

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


    @Query("""
            SELECT DISTINCT s FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN FETCH s.audioStorageKey
            LEFT JOIN FETCH s.album
            WHERE s.publiclyVisible = true
            """)
    List<Song> findAllPublic();

    @Query("""
            SELECT DISTINCT s FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN FETCH s.audioStorageKey
            LEFT JOIN FETCH s.album
            WHERE s.author.id = :userId
            OR s.publiclyVisible = true
            """)
    List<Song> findAllPublicOrOwnedByUser(@Param("userId") Long userId);

    @Query(value = """
            SELECT DISTINCT s
            FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN FETCH s.album
            JOIN s.genres g
            WHERE g = :genre
              AND (s.author.id = :userId OR s.publiclyVisible = true)
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s)
            FROM Song s
            JOIN s.genres g
            WHERE g = :genre
              AND (s.author.id = :userId OR s.publiclyVisible = true)
            """)
    Page<Song> findPublicOrOwnedByUserByGenre(@Param("genre") Genre genre,
                                              @Param("userId") Long userId,
                                              Pageable pageable);

    @Query(value = """
            SELECT DISTINCT s
            FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN FETCH s.album
            JOIN s.genres g
            WHERE g = :genre
              AND s.publiclyVisible = true
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s)
            FROM Song s
            JOIN s.genres g
            WHERE g = :genre
              AND s.publiclyVisible = true
            """)
    Page<Song> findPublicByGenre(
            @Param("genre") Genre genre,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true) // czyszczenie cachu hibernate
    @Query("UPDATE Song s SET s.viewCount = s.viewCount + :count WHERE s.id = :id")
    void incrementViewCountBy(@Param("id") Long id, @Param("count") Long count);

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

    @Query(
            value = """
        SELECT DISTINCT s
        FROM SongReaction r
        JOIN r.song s
        LEFT JOIN FETCH s.author
        WHERE r.user.id = :userId
          AND r.reactionType = 'FAVOURITE'
        """,
            countQuery = """
        SELECT COUNT(DISTINCT s)
        FROM SongReaction r
        JOIN r.song s
        WHERE r.user.id = :userId
          AND r.reactionType = 'FAVOURITE'
        """
    )
    Page<Song> findAllFavouriteByAppUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT percentile_disc(0.9) WITHIN GROUP (ORDER BY view_count) FROM songs", nativeQuery = true)
    Optional<Long> findViewCountPercentile90();

//    @Query("""
//        SELECT DISTINCT s.id
//        FROM SongReaction r
//        JOIN r.song s
//        WHERE r.user.id = :userId
//          """)
//    List<Long> findAllSongIdsReactedByUserByAppUserId(@Param("userId") Long userId);

    /// metoda zwraca piosenki:
    /// - posiadajace w sobie przynajmniej jeden genre z podanych
    /// - nalezace do podanych autorow
    ///
    /// zwracane piosenki to 'kandydaci' do wytworzenia listy rekomendowanych utworów.
    @Query("""
            SELECT DISTINCT s
            FROM Song s
            JOIN s.genres g
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.genres
            WHERE (g IN :genres OR s.author.id IN :authorIds)
              AND s.publiclyVisible = true
              AND s.id NOT IN (
                  SELECT r.song.id FROM SongReaction r
                  WHERE r.user.id = :userId
              )
            ORDER BY s.viewCount DESC
            """)
    List<Song> findCandidates(@Param("genres") Collection<Genre> genres, @Param("authorIds") Collection<Long> authorIds,
                              @Param("userId") Long userId, Pageable pageable);

    @Query(value = """
    SELECT s.id,
           s.title,
           u.id AS author_id,
           u.login AS author_username,
           s.album_id,
           string_agg(DISTINCT g.genre, ',' ORDER BY g.genre) AS genres_str,
           s.publicly_visible,
           s.created_at,
           sk.id AS cover_storage_key_id,
           COALESCE(vv.views, 0) AS view_count,
           COALESCE(rr.likes, 0) AS likes_count,
           (COALESCE(vv.views,0) + COALESCE(rr.likes,0) * :likeWage) AS score
    FROM songs s
    LEFT JOIN app_users u ON u.id = s.user_id
    LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
    LEFT JOIN song_genres g ON g.song_id = s.id

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
    -- AND (COALESCE(vv.views,0) + COALESCE(rr.likes,0)) > 0

    GROUP BY s.id, s.title, u.id, u.login, s.album_id, s.publicly_visible, s.created_at, sk.id, vv.views, rr.likes
    ORDER BY score DESC, s.created_at DESC
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
      -- AND (COALESCE(vv.views,0) + COALESCE(rr.likes,0)) > 0
    ) t
    """,
            nativeQuery = true)
    Page<SongProjection> findTrendingSongs(@Param("cutoffDate") Instant cutoffDate, Pageable pageable, @Param("likeWage") int likeWage);


    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   s.album_id,
                   string_agg(DISTINCT g.genre, ',' ORDER BY g.genre) AS genresStr,
                   s.publicly_visible,
                   s.created_at,
                   sk.id AS cover_storage_key_id,
                   COUNT(r.id) AS likes_count,
                   s.view_count
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            LEFT JOIN song_genres g ON g.song_id = s.id
            JOIN song_reactions r ON s.id = r.song_id AND r.reaction_type = 'LIKE'
            
            WHERE s.publicly_visible = true
            
            GROUP BY s.id, s.title, u.id, u.login, s.album_id, s.publicly_visible, s.created_at, sk.id, s.view_count
            
            ORDER BY likes_count DESC, s.view_count DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s.id)
            FROM songs s
            JOIN song_reactions r ON s.id = r.song_id AND r.reaction_type = 'LIKE'
            WHERE s.publicly_visible = true
            """,
            nativeQuery = true)
    Page<SongProjection> findTopLikedSongs(Pageable pageable);

    @Query(value = """
    SELECT s
    FROM Song s
    LEFT JOIN FETCH s.author
    LEFT JOIN FETCH s.coverStorageKey
    LEFT JOIN FETCH s.album
    WHERE s.publiclyVisible = true
    ORDER BY s.viewCount DESC
    """,
            countQuery = """
    SELECT COUNT(s)
    FROM Song s
    WHERE s.publiclyVisible = true
    """)
    Page<Song> findTopViewedSongs(Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.publiclyVisible = true ORDER BY s.viewCount DESC")
    List<Song> findTopPopularSongs(Pageable pageable);

    //todo do przerobienia zeby obsluzyc n+1 problem
    @Query("SELECT s FROM Song s WHERE s.publiclyVisible = true ORDER BY s.viewCount DESC")
    Page<Song> findTopPopularSongsPage(Pageable pageable);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   s.album_id,
                   COALESCE(g.genresStr, '') AS genresStr,
                   s.publicly_visible,
                   s.created_at,
                   sk.id AS cover_storage_key_id,
                   s.view_count,
                   (
                       CASE WHEN LOWER(s.title) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                       CASE WHEN LOWER(s.title) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                       CASE WHEN LOWER(s.title) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END
                   ) AS relevance_score
            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            LEFT JOIN (
                SELECT song_id, string_agg(DISTINCT genre, ',') AS genresStr
                FROM song_genres
                GROUP BY song_id
            ) g ON g.song_id = s.id
            WHERE (s.title ILIKE :containsQuery)
              AND (s.publicly_visible = true OR s.user_id = :userId)
            ORDER BY relevance_score DESC, s.view_count DESC;
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s.id)
            FROM songs s
            LEFT JOIN song_genres g ON g.song_id = s.id
            WHERE (s.title ILIKE :containsQuery)
              AND (s.publicly_visible = true OR s.user_id = :userId)
            """,
            nativeQuery = true)
    Page<SongProjection> searchSongs(
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
                   s.album_id,
                   COALESCE(g.genresStr, '') AS genresStr,
                   s.publicly_visible,
                   s.created_at,
                   sk.id AS cover_storage_key_id,
                   s.view_count,
                   (
                       CASE WHEN LOWER(s.title) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                       CASE WHEN LOWER(s.title) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                       CASE WHEN LOWER(s.title) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END
                   ) AS relevance_score

            FROM songs s
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
                        LEFT JOIN (
                SELECT song_id, string_agg(DISTINCT genre, ',') AS genresStr
                FROM song_genres
                GROUP BY song_id
            ) g ON g.song_id = s.id
            
            WHERE (s.title ILIKE :containsQuery)
              AND s.publicly_visible = true
            
            ORDER BY relevance_score DESC, s.view_count DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT s.id)
            FROM songs s
            LEFT JOIN song_genres g ON g.song_id = s.id
            WHERE (s.title ILIKE :containsQuery)
              AND s.publicly_visible = true
            """,
            nativeQuery = true)
    Page<SongProjection> searchSongsPublic(
            @Param("exactQuery") String exactQuery,
            @Param("startsWithQuery") String startsWithQuery,
            @Param("containsQuery") String containsQuery,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Song s
        SET s.likesCount = s.likesCount + (CASE WHEN :reactionType = 'LIKE' THEN 1 ELSE 0 END),
            s.dislikesCount = s.dislikesCount + (CASE WHEN :reactionType = 'DISLIKE' THEN 1 ELSE 0 END)
        WHERE s.id = :songId
    """)
    void incrementReactionCount(@Param("songId") Long songId, @Param("reactionType") String reactionType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Song s
        SET s.likesCount = GREATEST(0, s.likesCount - (CASE WHEN :reactionType = 'LIKE' THEN 1 ELSE 0 END)),
            s.dislikesCount = GREATEST(0, s.dislikesCount - (CASE WHEN :reactionType = 'DISLIKE' THEN 1 ELSE 0 END))
        WHERE s.id = :songId
    """)
    void decrementReactionCount(@Param("songId") Long songId, @Param("reactionType") String reactionType);

}

