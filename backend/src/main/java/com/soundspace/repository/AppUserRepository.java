package com.soundspace.repository;

import com.soundspace.dto.projection.AppUserProjection;
import com.soundspace.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByLogin(String login);
    @Query("select u.id from AppUser u where u.email = :email")
    Optional<Long> findUserIdByEmail(String email);

    @Query(value = """
            SELECT u.id,
                   u.login,
                   u.email,
                   u.sex,
                   u.role,
                   u.bio,
                   u.auth_provider,
                   u.created_at,
                   u.email_verified,
                   sk.id AS avatar_storage_key_id,

                   (CASE WHEN LOWER(u.login) = LOWER(:exactQuery) THEN 100 ELSE 0 END +
                    CASE WHEN LOWER(u.login) LIKE LOWER(:startsWithQuery) THEN 50 ELSE 0 END +
                    CASE WHEN LOWER(u.login) LIKE LOWER(:containsQuery) THEN 20 ELSE 0 END)
                                   AS relevance_score

            FROM app_users u
            LEFT JOIN storage_keys sk ON sk.id = u.avatar_storage_key_id
            WHERE (u.login ILIKE :containsQuery)
            
            ORDER BY relevance_score DESC, u.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM app_users u
            WHERE (u.login ILIKE :containsQuery)
            """,
            nativeQuery = true)
    Page<AppUserProjection> searchAppUser(
            @Param("exactQuery") String exactQuery,
            @Param("startsWithQuery") String startsWithQuery,
            @Param("containsQuery") String containsQuery,
            Pageable pageable
    );

}
