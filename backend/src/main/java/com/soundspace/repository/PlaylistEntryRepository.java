package com.soundspace.repository;

import com.soundspace.dto.projection.PlaylistSongProjection;
import com.soundspace.entity.Playlist;
import com.soundspace.entity.PlaylistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistEntryRepository extends JpaRepository<PlaylistEntry, Long> {

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


    // usuwa piosenke ze wszystkich playlsit w ktorych ta piosenka jest
    @Modifying
    @Query("DELETE FROM PlaylistEntry pe WHERE pe.song.id = :songId")
    void deleteAllBySongId(@Param("songId") Long songId);

    // usuwa wszystkie piosenki z danej playlisty
    @Modifying
    @Query("DELETE FROM PlaylistEntry pe WHERE pe.playlist.id = :playlistId")
    void deleteAllByPlaylistId(@Param("playlistId") Long playlistId);

    ////////////////////////////// BULK DELETE USERA //////////////////////////////

    // zwraca liste potrzebna do wywolania renumberPlaylists
    @Query("""
        SELECT DISTINCT pe.playlist.id
        FROM PlaylistEntry pe
        WHERE pe.song.author.id = :userId
          AND pe.playlist.creator.id != :userId
    """)
    List<Long> findPlaylistIdsToRepair(@Param("userId") Long userId);

    // - usuwa kazda piosenke danego usera ze wszystkich playlsit w ktorych ta piosenka jest
    // - usuwa tez wszystkie piosenki z jego playlist
    @Modifying
    @Query("DELETE FROM PlaylistEntry pe WHERE pe.song.author.id = :userId OR pe.playlist.creator.id = :userId")
    void deleteEntriesBySongAuthorId(@Param("userId") Long userId);

    // renumber playlist z ktorych usunieto piosenki i maja dziury cn
    @Modifying
    @Query(value = """
        UPDATE playlist_entries pe
        SET position = new_ranking.rn - 1
        FROM (
            SELECT id, ROW_NUMBER() OVER (PARTITION BY playlist_id ORDER BY position ASC) as rn
            FROM playlist_entries
            WHERE playlist_id IN :playlistIds
        ) new_ranking
        WHERE pe.id = new_ranking.id
    """, nativeQuery = true)
    void renumberPlaylists(@Param("playlistIds") List<Long> playlistIds);

    Optional<PlaylistEntry> findBySongId(Long songId);
}
