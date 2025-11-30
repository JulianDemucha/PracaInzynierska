package com.soundspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateAlbumRequest {
    @NotBlank(message = "Tytuł jest wymagany")
    @Size(max = 64, message = "Tytuł jest za długi")
    private String title;

    @NotBlank
    @NotNull
    private String description;
    private boolean publiclyVisible;
    private List<String> genre;
    @NotNull(message = "Plik okładki jest wymagany")
    private MultipartFile coverFile;
}
