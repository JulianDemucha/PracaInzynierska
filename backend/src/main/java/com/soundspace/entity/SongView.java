package com.soundspace.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.Instant;

@Entity
@Table(name = "song_views")
@Check(constraints = "user_id IS NOT NULL OR ip_address IS NOT NULL")
@Setter
public class SongView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // nullable true bo niezalogowany moze byc
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;

    private String ipAddress; // dla niezalogowanego po ip bedzie sprawdzane

    private Instant viewedAt = Instant.now();

    //todo: przeniesc do unit testu pozniej
    @AssertTrue(message = "Wyświetlenie musi mieć przypisanego użytkownika lub adres IP")
    private boolean isUserOrIpPresent() {
        return user != null || (ipAddress != null && !ipAddress.isBlank());
    }
}