package com.soundspace.repository;

import com.soundspace.entity.PlaylistEntry;
import org.springframework.data.repository.Repository;
import java.util.Optional;

public interface PlaylistEntryRepository extends Repository<PlaylistEntry, Long> {

    PlaylistEntry save(PlaylistEntry playlistEntry);

    Optional<PlaylistEntry> findBySongIdAndPlaylistId(Long songId, Long playlistId);
}
