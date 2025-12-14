package com.soundspace.repository;

import com.soundspace.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByLogin(String login);
    @Query("select u.id from AppUser u where u.email = :email")
    Optional<Long> findUserIdByEmail(String email);
}
