package com.soundspace.service;

import com.soundspace.entity.AppUser;
import com.soundspace.entity.SongReaction;
import com.soundspace.enums.ReactionType;
import com.soundspace.repository.SongReactionRepository;
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
/*
    todo:
     - dodawanie like/dislike
     - dodawanie favourite
     - usuwanie liek/dislike/favourite
        *like i dislike nie mogą współistnieć, ale favourite jest niezależne

 */
public class ReactionService {
    private final SongReactionRepository songReactionRepository;
    private final SongCoreService songCoreService;
    private final AppUserService appUserService;

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
            return;
        }

        songReaction = songReactionOpt.get();
        ReactionType existingReactionType = songReaction.getReactionType();

        if (existingReactionType == requestReactionType) return;

        // jezeli istnieje juz favourite (revert zwraca favourite jezeli dany favourite)
        if (existingReactionType == revertReactionType(existingReactionType)) return;

        if (existingReactionType == revertReactionType(requestReactionType)) {
            songReaction.setReactionType(requestReactionType);
            songReaction.setReactedAt(Instant.now());
            songReactionRepository.save(songReaction);
        }

    }

    @Transactional
    public void deleteLikeOrDislike(Long songId, UserDetails userDetails) {
        AppUser appUser = appUserService.getUserByEmail(userDetails.getUsername());
        Long appUserId = appUser.getId();
        songReactionRepository.deleteLikeOrDislikeBySongIdAndUserId(songId, appUserId);
    }

    @Transactional
    public void deleteFavourite(Long songId, UserDetails userDetails) {
        AppUser appUser = appUserService.getUserByEmail(userDetails.getUsername());
        Long appUserId = appUser.getId();
        songReactionRepository.deleteFavouriteBySongIdAndUserId(songId, appUserId);
    }


    /// helpers

    private ReactionType revertReactionType(ReactionType reactionType) {
        return switch (reactionType) {
            case LIKE -> ReactionType.DISLIKE;
            case DISLIKE -> ReactionType.LIKE;
            case FAVOURITE -> ReactionType.FAVOURITE;
        };
    }

}
