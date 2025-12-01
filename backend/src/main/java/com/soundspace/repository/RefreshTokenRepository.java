package com.soundspace.repository;

import com.soundspace.entity.AppUser;
import com.soundspace.entity.RefreshToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String token);

    @Modifying
    @Query("delete from RefreshToken t where t.revoked = true and t.revokedAt < :cutoff")
    int deleteRevokedBeforeCutoff(@Param("cutoff") Instant cutoff);

    void deleteAllByAppUserId(Long appUserId);
}
