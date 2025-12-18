package com.soundspace.service.song;

import com.soundspace.entity.AppUser;
import com.soundspace.entity.SongReaction;
import com.soundspace.enums.ReactionType;
import com.soundspace.repository.SongReactionRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.service.user.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionService {
    private final SongReactionRepository songReactionRepository;
    private final SongCoreService songCoreService;
    private final AppUserService appUserService;
    private final SongRepository songRepository;

    @Transactional
    public void addReaction(Long songId, ReactionType requestReactionType, UserDetails userDetails) {
        AppUser appUser = appUserService.getUserByEmail(userDetails.getUsername());
        Long appUserId = appUser.getId();

        SongReaction songReaction;
        Optional<SongReaction> songReactionOpt;

        // jezeli like albo dislike to go szuka, a jezeli favourite to tez go szuka
        // to jest po to ze jezeli istnieje np like i favourite to samo findBySongId rzuciloby blad
        if(requestReactionType == ReactionType.LIKE || requestReactionType == ReactionType.DISLIKE) {
            songReactionOpt = songReactionRepository.findLikeOrDislikeBySongIdAndUserId(songId, appUserId);
        } else songReactionOpt = songReactionRepository.findFavoriteBySongIdAndUserId(songId, appUserId);

        if (songReactionOpt.isEmpty()) {
            songReaction = new SongReaction();
            songReaction.setReactionType(requestReactionType);
            songReaction.setSong(songCoreService.getReferenceById(songId));
            songReaction.setUser(appUser);
            // reactedAt automatycznie sie ustawi
            songReactionRepository.save(songReaction);
            songRepository.incrementReactionCount(songId, requestReactionType.toString());
            return;
        }

        songReaction = songReactionOpt.get();
        ReactionType existingReactionType = songReaction.getReactionType();

        if (existingReactionType == requestReactionType) return;

        // jezeli istnieje juz favourite (revert zwraca favourite jezeli dany favourite)
        if (existingReactionType == revertReactionType(existingReactionType)) return;

        // swap like <--> dislike
        if (existingReactionType == revertReactionType(requestReactionType)) {
            songReaction.setReactionType(requestReactionType);
            songReaction.setReactedAt(Instant.now());

            songReactionRepository.save(songReaction);

            songRepository.decrementReactionCount(songId, existingReactionType.toString());
            songRepository.incrementReactionCount(songId, requestReactionType.toString());
        }

    }

    @Transactional
    public void deleteLikeOrDislike(Long songId, UserDetails userDetails) {
        AppUser appUser = appUserService.getUserByEmail(userDetails.getUsername());
        Long appUserId = appUser.getId();
        Optional<ReactionType> reactionTypeOpt = songReactionRepository.findTypeBySongIdAndUserId(songId, appUserId);

        if(reactionTypeOpt.isEmpty()) { return;}
        ReactionType reactionType = reactionTypeOpt.get();

        if (reactionType != ReactionType.FAVOURITE){
        songReactionRepository.deleteLikeOrDislikeBySongIdAndUserId(songId, appUserId);
        songRepository.decrementReactionCount(songId, reactionType.toString());
        }
    }

    @Transactional
    public void deleteFavourite(Long songId, UserDetails userDetails) {
        AppUser appUser = appUserService.getUserByEmail(userDetails.getUsername());
        Long appUserId = appUser.getId();
        songReactionRepository.deleteFavouriteBySongIdAndUserId(songId, appUserId);
    }


    @Transactional
    public void deleteAllBySongId(Long songId) {
        songReactionRepository.deleteAllBySongId(songId);
    }

    // helpers

    private ReactionType revertReactionType(ReactionType reactionType) {
        return switch (reactionType) {
            case LIKE -> ReactionType.DISLIKE;
            case DISLIKE -> ReactionType.LIKE;
            case FAVOURITE -> ReactionType.FAVOURITE;
        };
    }

}
