package com.soundspace.repository;

import com.soundspace.entity.SongView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface SongViewRepository extends JpaRepository<SongView, Long> {

    // dla zalogowanego uzytkownika
    boolean existsBySongIdAndUserIdAndViewedAtAfter(Long songId, Long userId, Instant cutoff);

    // dla niezalogowanego
    boolean existsBySongIdAndIpAddressAndViewedAtAfter(Long songId, String ipAddress, Instant cutoff);

    @Modifying
    @Query("UPDATE SongView v SET v.user = NULL WHERE v.user.id = :userId")
    void detachUserFromViews(@Param("userId") Long userId);
}