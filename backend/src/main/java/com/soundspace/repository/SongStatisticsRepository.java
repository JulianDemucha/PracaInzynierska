package com.soundspace.repository;

import com.soundspace.entity.SongStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongStatisticsRepository extends JpaRepository<SongStatistics, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SongStatistics s SET s.viewCount = s.viewCount + :count WHERE s.id = :id")
    void incrementViewCountBy(@Param("id") Long id, @Param("count") Long count);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE SongStatistics s
           SET s.likesCount = s.likesCount + (CASE WHEN :reactionType = 'LIKE' THEN 1 ELSE 0 END),
               s.dislikesCount = s.dislikesCount + (CASE WHEN :reactionType = 'DISLIKE' THEN 1 ELSE 0 END)
           WHERE s.id = :songId
           """)
    void incrementReactionCount(@Param("songId") Long songId, @Param("reactionType") String reactionType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE SongStatistics s
            SET s.likesCount = GREATEST(0, s.likesCount - (CASE WHEN :reactionType = 'LIKE' THEN 1 ELSE 0 END)),
                s.dislikesCount = GREATEST(0, s.dislikesCount - (CASE WHEN :reactionType = 'DISLIKE' THEN 1 ELSE 0 END))
            WHERE s.id = :songId
            """)
    void decrementReactionCount(@Param("songId") Long songId, @Param("reactionType") String reactionType);

    @Query(value = "SELECT percentile_disc(0.9) WITHIN GROUP (ORDER BY view_count) FROM song_statistics", nativeQuery = true)
    Optional<Long> findViewCountPercentile90();
}