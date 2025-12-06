package com.soundspace.repository;

import com.soundspace.entity.SongView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface SongViewRepository extends JpaRepository<SongView, Long> {

    // dla zalogowanego uzytkownika
    boolean existsBySongIdAndUserIdAndViewedAtAfter(Long songId, Long userId, Instant cutoff);

    // dla niezalogowanego
    boolean existsBySongIdAndIpAddressAndViewedAtAfter(Long songId, String ipAddress, Instant cutoff);
}