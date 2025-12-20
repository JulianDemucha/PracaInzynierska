package com.soundspace.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "song_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongStatistics {

    @Id
    @Column(name = "song_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // to samo id z songiem (klucz glowny jest tez kluczem obcym)
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Column(name = "dislikes_count", nullable = false)
    private Integer dislikesCount = 0;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    
    public int getLikesCount() {
        return likesCount == null ? 0 : likesCount;
    }

    public int getDislikesCount() {
        return dislikesCount == null ? 0 : dislikesCount;
    }

    public long getViewCount() {
        return viewCount == null ? 0 : viewCount;
    }
}