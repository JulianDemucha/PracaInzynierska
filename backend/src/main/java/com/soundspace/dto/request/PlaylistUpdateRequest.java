package com.soundspace.dto.request;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record PlaylistUpdateRequest (
        @Size(min = 1, max = 64)
        String title,
        MultipartFile coverFile,
        Boolean publiclyVisible
){}
