package com.soundspace.service.song;

import com.soundspace.dto.SongBaseDto;
import com.soundspace.dto.SongDtoWithDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CachedRecommendationsProvider {

    private final RecommendationsService recommendationsService;

    @Cacheable(value = "recommendations", key = "(#userDetails == null ? 'ANON' : #userDetails.username)")
    public List<SongBaseDto> getRecommendations(UserDetails userDetails) {
        return recommendationsService.getRecommendations(userDetails);
    }
}