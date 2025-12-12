package com.soundspace.repository;

import com.soundspace.dto.projection.RecommendationsSongProjection;
import com.soundspace.dto.projection.SongProjection;
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

    @Query("""
            SELECT DISTINCT s
            FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN FETCH s.album
            JOIN s.genres g
            WHERE g = :genre
              AND (s.author.id = :userId OR s.publiclyVisible = true)
            """)
    List<Song> findPublicOrOwnedByUserByGenre(@Param("genre") Genre genre, @Param("userId") Long userId);

    @Query("""
            SELECT DISTINCT s
            FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN FETCH s.album
            JOIN s.genres g
            WHERE g = :genre
              AND s.publiclyVisible = true
            """)
    List<Song> findPublicByGenre(@Param("genre") Genre genre);

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
    Long findViewCountPercentile90();

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

    @Query("""
            SELECT s
            FROM Song s
            LEFT JOIN FETCH s.author
            LEFT JOIN FETCH s.coverStorageKey
            LEFT JOIN FETCH s.album
            JOIN SongReaction r ON r.song.id = s.id
            WHERE r.reactionType = 'LIKE'
              AND r.reactedAt > :cutoffDate
              AND s.publiclyVisible = true
            GROUP BY s, s.author, s.coverStorageKey, s.album
            ORDER BY COUNT(r) DESC, s.viewCount DESC
            """)
    List<Song> findTopLikedSongsSinceCutoff(@Param("cutoffDate") Instant cutoffDate, Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.publiclyVisible = true ORDER BY s.viewCount DESC")
    List<Song> findTopPopularSongs(Pageable pageable);

    @Query("SELECT s FROM Song s WHERE s.publiclyVisible = true ORDER BY s.viewCount DESC")
    Page<Song> findTopPopularSongsPage(Pageable pageable);
}

