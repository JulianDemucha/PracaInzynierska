package com.soundspace.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongUploadRequest {

    @NotNull(message = "Plik jest wymagany")
    private MultipartFile file;

    @NotBlank
    @Size(max = 32)
    private String title;

    @NotBlank
    @Size(max = 32)
    private String genre;

    @NotNull
    private Long userId;

    private Boolean publiclyVisible = false;
}

