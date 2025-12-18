package com.soundspace.repository;

import com.soundspace.entity.Song;
import com.soundspace.entity.SongReaction;
import com.soundspace.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface SongReactionRepository extends JpaRepository<SongReaction, Long> {


    @Query(value = """
            SELECT s FROM SongReaction s
            WHERE s.song.id = :songId
            AND s.user.id = :userId
            AND s.reactionType IN ('LIKE', 'DISLIKE')
            """)
    Optional<SongReaction> findLikeOrDislikeBySongIdAndUserId(@Param("songId")Long songId, @Param("userId")Long userId);

    @Query(value = """
            SELECT s FROM SongReaction s
            WHERE s.song.id = :songId
            AND s.user.id = :userId
            AND s.reactionType = 'FAVOURITE'
            """)
    Optional<SongReaction> findFavoriteBySongIdAndUserId(@Param("songId")Long songId, @Param("userId")Long userId);

    @Modifying
    @Query("DELETE FROM SongReaction s WHERE s.song.id = :songId AND s.user.id = :userId AND s.reactionType IN ('LIKE', 'DISLIKE')")
    void deleteLikeOrDislikeBySongIdAndUserId(@Param("songId") Long songId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM SongReaction s WHERE s.song.id = :songId AND s.user.id = :userId AND s.reactionType = 'FAVOURITE'")
    void deleteFavouriteBySongIdAndUserId(@Param("songId")Long songId, @Param("userId")Long userId);

    @Query("""
            SELECT s.reactionType
            FROM SongReaction s
            WHERE s.song.id = :songId
              AND s.user.id = :userId
            """)
    Optional<ReactionType> findTypeBySongIdAndUserId(
            @Param("songId") Long songId,
            @Param("userId") Long userId
    );

    // do bulk delete usera
    // usuwa wszystkie reakcje usera i reakcje na jego pisoenkach
    @Modifying
    @Query("""
        DELETE FROM SongReaction r
        WHERE r.user.id = :userId
           OR r.song.author.id = :userId
    """)
    void deleteAllRelatedToUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM SongReaction r WHERE r.song.id = :songId")
    void deleteAllBySongId(@Param("songId") Long songId);

    Long song(Song song);
}
