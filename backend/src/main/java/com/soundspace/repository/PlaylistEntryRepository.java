package com.soundspace.repository;

import com.soundspace.entity.PlaylistEntry;
import org.springframework.data.repository.Repository;

public interface PlaylistEntryRepository extends Repository<PlaylistEntry, Long> {

    PlaylistEntry save(PlaylistEntry playlistEntry);

    boolean existsBySongIdAndPlaylistId(Long songId, Long playlistId);
}
