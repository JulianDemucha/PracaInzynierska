package com.soundspace.repository;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {
        @Query(value = """
        SELECT 
          s.id,
          s.title,
          u.login AS author_login,
          s.album_id,
          string_agg(g.genre, ',') AS genresStr,
          s.publicly_visible,
          s.created_at,
          s.cover_storage_key
        FROM songs s
        LEFT JOIN app_users u ON u.id = s.user_id
        LEFT JOIN song_genres g ON g.song_id = s.id
        WHERE s.user_id = :userId
        GROUP BY s.id, u.login, s.album_id, s.publicly_visible, s.created_at, s.cover_storage_key
        """, nativeQuery = true)
        List<SongProjection> findSongsByUserNative(@Param("userId") Long userId);



}
