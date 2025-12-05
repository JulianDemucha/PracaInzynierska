package com.soundspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record PlaylistCreateRequest(
        @NotBlank(message = "Nazwa playlisty nie może być pusta")
        @NotNull
        @Size(min = 1, max = 50, message = "Nazwa playlisty musi mieć od 1 do 50 znaków")
        String title,

        @NotNull(message = "Widoczność musi być określona")
        Boolean publiclyVisible,

        MultipartFile coverFile
) {}