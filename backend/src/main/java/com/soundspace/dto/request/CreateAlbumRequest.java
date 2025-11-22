package com.soundspace.dto.request;

import lombok.Data;

@Data
public class CreateAlbumRequest {
    private String title;
    private String description;
    private Long authorId;
    private boolean publiclyVisible;
}
