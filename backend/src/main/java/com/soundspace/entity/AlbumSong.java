package com.soundspace.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "album_songs")
public class AlbumSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "track_position", nullable = false)
    private int trackPosition;


}
