package com.soundspace.dto.projection;

import com.soundspace.enums.Genre;
import java.util.List;

public interface RecommendationsSongProjection {
    Long getId();
    List<Genre> getGenres(); // jpql, wiec nie trzeba pobierac str
    Long getAuthorId();
}