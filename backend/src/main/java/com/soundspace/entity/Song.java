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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @Size(max = 3)
    @ElementCollection(targetClass = Genre.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "song_genres", joinColumns = @JoinColumn(name = "song_id"))
    @Column(name = "genre", nullable = false)
    private List<Genre> genres = new ArrayList<>();

    @Column(name = "audio_storage_key")
    private String audioStorageKey;

    @Column(name = "cover_storage_key")
    private String coverStorageKey;

    @Column(name = "audio_size_bytes")
    private long audioSizeBytes;

    @Column(name= "cover_size_bytes")
    private long coverSizeBytes;

    @Column(name = "audio_file_mime_type")
    private String audioFileMimeType;

    @Column(name = "cover_file_mime_type")
    private String coverFileMimeType;

    @Column(name = "publicly_visible", nullable = false)
    private Boolean publiclyVisible;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
