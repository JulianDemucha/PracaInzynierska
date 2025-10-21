package com.soundspace.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private AppUser user;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", unique = true)
//    private Song song;


}
