package com.soundspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongUploadRequest {

    @NotNull(message = "Plik audio (m4a) jest wymagany")
    private MultipartFile audioFile;

    @NotNull(message = "Plik okładki jest wymagany")
    private MultipartFile coverFile;

    @NotBlank
    @Size(max = 32)
    private String title;

    @Size(min = 1, max = 3)
    private List<String> genre;

    @NotNull(message = "Widoczność musi być określona")
    private Boolean publiclyVisible = false;
}

