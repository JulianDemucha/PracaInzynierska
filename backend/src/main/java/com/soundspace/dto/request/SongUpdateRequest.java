package com.soundspace.dto.request;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record SongUpdateRequest (
        @Size(max = 32)
        String title,
        Boolean publiclyVisible,
        MultipartFile coverFile
) {}
