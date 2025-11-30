package com.soundspace.entity;

import com.soundspace.enums.Visibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser creator;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Setter
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Setter
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Getter
    @OneToMany(
            mappedBy = "playlist",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("position ASC")
    private List<PlaylistEntry> songs = new ArrayList<>();

    public void addSong(Song song) {
        PlaylistEntry entry = new PlaylistEntry();
        entry.setPlaylist(this);
        entry.setSong(song);
        entry.setPosition(this.songs.size());
        this.songs.add(entry);
    }

}
