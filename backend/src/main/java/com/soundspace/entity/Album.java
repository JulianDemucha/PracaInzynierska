package com.soundspace.entity;

import com.soundspace.enums.Genre;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Table(name = "albums")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Album {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    private List<Song> songs = new ArrayList<>();

    @Column
    private String title;

    @Size(max = 3)
    @ElementCollection(targetClass = Genre.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "album_genres", joinColumns = @JoinColumn(name = "album_id"))
    @Column(name = "genre", nullable = false)
    private List<Genre> genres = new ArrayList<>();

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser author;

    @Column(nullable = false)
    private Boolean publiclyVisible;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

}
