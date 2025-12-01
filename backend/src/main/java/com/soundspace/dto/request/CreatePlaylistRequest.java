package com.soundspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CreatePlaylistRequest(
        @NotBlank(message = "Nazwa playlisty nie może być pusta")
        @Size(min = 1, max = 50, message = "Nazwa playlisty musi mieć od 1 do 50 znaków")
        String name,

        @NotNull(message = "Widoczność musi być określona")
        Boolean publiclyVisible,

        @NotNull(message = "Plik okładki jest wymagany")
        MultipartFile coverFile
) {}