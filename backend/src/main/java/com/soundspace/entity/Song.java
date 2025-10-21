package com.soundspace.entity;

import com.soundspace.enums.Visibility;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String title;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="artist_id")
//    private Artist artist;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="genere_id")
//    private Genere genere;

    @Column
    private String file_path;

    @Column
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @Column(name = "created_at")
    private Instant createdAt;

}
