package com.soundspace.entity;
import jakarta.persistence.*;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "song_views")
@Setter
public class SongView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // nullable true bo niezalogowany moze byc
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;

    private String ipAddress; // dla niezalogowanego po ip bedzie sprawdzane

    private Instant viewedAt = Instant.now();
}