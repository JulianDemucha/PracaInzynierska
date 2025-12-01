package com.soundspace.repository;

import com.soundspace.dto.projection.PlaylistSongProjection;
import com.soundspace.entity.PlaylistEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistEntryRepository extends Repository<PlaylistEntry, Long> {

    PlaylistEntry save(PlaylistEntry playlistEntry);

    boolean existsBySongIdAndPlaylistId(Long songId, Long playlistId);

    @Query(value = """
            SELECT s.id,
                   s.title,
                   u.id AS author_id,
                   u.login AS author_username,
                   s.album_id,
                   string_agg(g.genre, ',') AS genresStr,
                   s.publicly_visible,
                   s.created_at,
                   sk.id AS cover_storage_key_id,
                   pe.position AS position
            FROM playlist_entries pe
            JOIN songs s ON s.id = pe.song_id
            LEFT JOIN app_users u ON u.id = s.user_id
            LEFT JOIN song_genres g ON g.song_id = s.id
            LEFT JOIN storage_keys sk ON sk.id = s.cover_storage_key_id
            WHERE pe.playlist_id = :playlistId
            GROUP BY s.id, u.id, s.album_id, s.publicly_visible, s.created_at, sk.id, pe.position
            ORDER BY pe.position ASC
            """, nativeQuery = true)
    List<PlaylistSongProjection> findAllSongsInPlaylist(@Param("playlistId") Long playlistId);
}
