package com.soundspace.entity;

import jakarta.persistence.*;
import jdk.jfr.BooleanFlag;

import java.time.Instant;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser artist;

    @Column(length = 1000)
    private String bio;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "verified", nullable = false)
    @BooleanFlag
    private boolean artistVerified = false;

}
