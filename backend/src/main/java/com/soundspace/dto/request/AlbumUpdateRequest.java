package com.soundspace.dto.request;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record AlbumUpdateRequest (
        @Size(min = 1, max = 64)
        String title,
        @Size(min = 1, max = 64)
        String description,
        MultipartFile coverFile,
        Boolean publiclyVisible
){}
