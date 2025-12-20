package com.soundspace.service;

import com.soundspace.dto.*;
import com.soundspace.dto.projection.*;
import com.soundspace.repository.AlbumRepository;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.repository.PlaylistRepository;
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
    private final AlbumRepository albumRepository;

    private final AppUserService appUserService;
    private final PlaylistRepository playlistRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public Page<SongBaseDto> searchSongs(String query, Pageable pageable, UserDetails userDetails) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }

        String cleanedQuery = query.trim();
        String startsWith = cleanedQuery + "%";

        String contains = "%" + cleanedQuery + "%";
        Page<SongBaseProjection> results = userDetails == null ?
                songRepository.searchSongsPublic(
                cleanedQuery, //exact
                startsWith,
                contains,
                pageable
        ) :
                songRepository.searchSongs(
                cleanedQuery,
                startsWith,
                contains,
                appUserService.getUserIdByEmail(userDetails.getUsername()),
                pageable
        );

        return results.map(SongBaseDto::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDto> searchAlbums(String query, Pageable pageable, UserDetails userDetails) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }

        String cleanedQuery = query.trim();
        String startsWith = cleanedQuery + "%";

        String contains = "%" + cleanedQuery + "%";
        Page<AlbumProjection> results = userDetails == null ?
                albumRepository.searchAlbumsPublic(
                cleanedQuery, //exact
                startsWith,
                contains,
                pageable
        ):
                albumRepository.searchAlbums(
                cleanedQuery,
                startsWith,
                contains,
                appUserService.getUserIdByEmail(userDetails.getUsername()),
                pageable
        );

        return results.map(AlbumDto::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PlaylistDto> searchPlaylists(String query, Pageable pageable, UserDetails userDetails) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }

        String cleanedQuery = query.trim();
        String startsWith = cleanedQuery + "%";

        String contains = "%" + cleanedQuery + "%";
        Page<PlaylistProjection> results = userDetails == null ?
                playlistRepository.searchPlaylistsPublic(
                        cleanedQuery, //exact
                        startsWith,
                        contains,
                        pageable
                ):
                playlistRepository.searchPlaylists(
                        cleanedQuery,
                        startsWith,
                        contains,
                        appUserService.getUserIdByEmail(userDetails.getUsername()),
                        pageable
                );

        return results.map(PlaylistDto::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AppUserDto> searchUsers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return Page.empty(pageable);
        }

        String cleanedQuery = query.trim();
        String startsWith = cleanedQuery + "%";

        String contains = "%" + cleanedQuery + "%";
        Page<AppUserProjection> results =
                appUserRepository.searchAppUser(
                        cleanedQuery, //exact
                        startsWith,
                        contains,
                        pageable
                );

        return results.map(AppUserDto::toDto);
    }

}
