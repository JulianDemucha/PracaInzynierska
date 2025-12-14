package com.soundspace.service;

import com.soundspace.dto.SongDto;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.repository.SongRepository;
import com.soundspace.service.user.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final SongRepository songRepository;
    private final AppUserService appUserService;

    @Transactional(readOnly = true)
    public Page<SongDto> searchSongs(String query, Pageable pageable, UserDetails userDetails) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }

        String cleanedQuery = query.trim();
        String startsWith = cleanedQuery + "%";

        String contains = "%" + cleanedQuery + "%";
        Page<SongProjection> results = userDetails == null ? songRepository.searchSongsPublic(
                cleanedQuery, //exact
                startsWith,
                contains,
                pageable
        ) : songRepository.searchSongs(
                cleanedQuery,
                startsWith,
                contains,
                appUserService.getUserIdByEmail(userDetails.getUsername()),
                pageable
        );

        return results.map(SongDto::toDto);
    }
}
