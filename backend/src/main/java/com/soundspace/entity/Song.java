package com.soundspace.entity;

import com.soundspace.enums.Genre;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "songs")
@Setter
@Getter
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @Size(max = 3)
    @ElementCollection(targetClass = Genre.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "song_genres", joinColumns = @JoinColumn(name = "song_id"))
    @Column(name = "genre", nullable = false)
    private List<Genre> genres = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_storage_key_id", nullable = false)
    private StorageKey audioStorageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_storage_key_id", nullable = false)
    private StorageKey coverStorageKey;

    @Column(name = "publicly_visible", nullable = false)
    private Boolean publiclyVisible;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
