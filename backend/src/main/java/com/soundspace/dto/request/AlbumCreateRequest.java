package com.soundspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AlbumCreateRequest {
    @NotBlank(message = "Tytuł jest wymagany")
    @Size(min = 2,max = 64, message = "Tytuł jest za długi")
    private String title;

    @NotBlank
    @NotNull
    @Size(min = 1, max = 64)
    private String description;

    @NotNull(message = "Widoczność musi być określona")
    private boolean publiclyVisible;

    @Size(min=1, max = 3)
    private List<String> genre;

    private MultipartFile coverFile;
}
