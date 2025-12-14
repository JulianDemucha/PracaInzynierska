package com.soundspace.dto.projection;

import com.soundspace.enums.Role;
import com.soundspace.enums.Sex;
import com.soundspace.enums.UserAuthProvider;

import java.time.Instant;

public interface AppUserProjection {
    Long getId();
    String getLogin();
    String getEmail();
    Sex getSex();
    Role getRole();
    UserAuthProvider getAuthProvider();
    Boolean getEmailVerified();
    String getBio();
    Instant getCreatedAt();
    Long getAvatarStorageKeyId();
}