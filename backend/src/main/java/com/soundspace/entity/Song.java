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

    @Column
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser author;

    @Size(max = 3)
    @ElementCollection(targetClass = Genre.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "song_genres", joinColumns = @JoinColumn(name = "song_id"))
    @Column(name = "genre", nullable = false)
    private List<Genre> genres = new ArrayList<>();

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "publicly_visible", nullable = false)
    private Boolean publiclyVisible;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
