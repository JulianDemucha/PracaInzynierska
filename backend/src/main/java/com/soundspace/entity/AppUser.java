package com.soundspace.entity;

import com.soundspace.enums.Role;
import com.soundspace.enums.UserAuthProvider;
import jakarta.persistence.*;
import jdk.jfr.BooleanFlag;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@Table(name = "app_user")
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true, nullable=false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private UserAuthProvider authProvider;

    @Column(nullable = false)
    @BooleanFlag
    private boolean emailVerified;

}
