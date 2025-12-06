package com.soundspace.repository;

import com.soundspace.entity.SongReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/*TODO:
    - sprawdzanie czy jest jakis like albo dislike
    - sprawdzanie czy jest jakis favourite

 */

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
}
