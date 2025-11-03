package com.soundspace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comment_likes")
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(name = "liked_at", nullable = false)
    private Instant likedAt = Instant.now();

}