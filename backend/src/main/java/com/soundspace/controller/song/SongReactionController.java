package com.soundspace.controller.song;
import com.soundspace.enums.ReactionType;
import com.soundspace.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongReactionController {
    private final ReactionService reactionService;

    @PostMapping("/{songId}/like")
    public ResponseEntity<Void> likeSong(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.addReaction(songId, ReactionType.LIKE, userDetails);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{songId}/dislike")
    public ResponseEntity<Void> dislikeSong(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.addReaction(songId, ReactionType.DISLIKE, userDetails);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{songId}/favourite")
    public ResponseEntity<Void> favouriteSong(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.addReaction(songId, ReactionType.FAVOURITE, userDetails);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{songId}/like")
    public ResponseEntity<Void> deleteLike(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.deleteLikeOrDislike(songId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{songId}/dislike")
    public ResponseEntity<Void> deleteDislike(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.deleteLikeOrDislike(songId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{songId}/favourite")
    public ResponseEntity<Void> deleteFavourite(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.deleteFavourite(songId, userDetails);
        return ResponseEntity.noContent().build();
    }


    /// HELPERY

}
