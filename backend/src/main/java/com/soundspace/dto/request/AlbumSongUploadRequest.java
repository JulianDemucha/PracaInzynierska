package com.soundspace.dto.request;

import com.soundspace.entity.Album;
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
public class AlbumSongUploadRequest {
    @NotNull(message = "Plik audio (m4a) jest wymagany")
    private MultipartFile audioFile;

    @NotBlank
    @Size(max = 32)
    private String title;

    private Long albumId;

}

