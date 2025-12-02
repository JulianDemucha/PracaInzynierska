package com.soundspace.repository;

import com.soundspace.entity.SongReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongReactionRepository extends JpaRepository<SongReaction, Long> {
}
