package com.soundspace.service;

import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.CreateAlbumRequest;
import com.soundspace.entity.Album;
import com.soundspace.entity.Song;
import com.soundspace.enums.Genre;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.AlbumNotFoundException;
import com.soundspace.repository.AlbumRepository;
import com.soundspace.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final AppUserService appUserService;
    private final SongCoreService songCoreService;
    private final SongRepository songRepository;

    public Optional<Album> findById(Long id) {
        if (id == null) return Optional.empty();
        return albumRepository.findById(id);
    }

    public AlbumDto getAlbumById(Long id, String userEmail) {
        Album album = findById(id).orElseThrow(
                () -> new AlbumNotFoundException(id));

        // jeżeli requestujący user nie jest autorem albumu i album jest prywatny - throw
        if (userEmail == null || (
                !album.getPubliclyVisible()
                &&
                !appUserService.getUserByEmail(userEmail).getId().equals(album.getAuthor().getId())
        )) throw new AccessDeniedException("Brak uprawnień");

        return AlbumDto.toDto(album);
    }

    public List<AlbumDto> findAllAlbumsByUserId(Long userId, String userEmail) {
        if(userEmail == null) throw new UsernameNotFoundException("Brak uprawnień");

        List<AlbumDto> albums = new java.util.ArrayList<>(albumRepository.findAllByAuthorId(userId)
                .stream()
                .map(AlbumDto::toDto)
                .toList());

        // jeżeli requestujący user nie jest tym samym co user w request'cie - remove niepubliczne utwory
        if(userId.equals(appUserService.getUserByEmail(userEmail).getId()))
            albums.removeIf(album -> !album.publiclyVisible());

        return albums;
    }

    public AlbumDto createAlbum(CreateAlbumRequest request, String userEmail) {
        // jeżeli requestujący user nie jest tym samym co user w request'cie - throw
        if (userEmail == null || !appUserService.getUserByEmail(userEmail).getId()
                .equals(request.getAuthorId()))
            throw new AccessDeniedException("Brak uprawnień");


        if (request.getTitle() == null || request.getTitle().isBlank())
            throw new IllegalArgumentException("Tytuł nie moze byc pusty");

        if (request.getDescription() == null || request.getDescription().isBlank())
            request.setDescription(request.getTitle());

        Album album = Album.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(appUserService.getUserById(request.getAuthorId()))
                .publiclyVisible(request.isPubliclyVisible())
                .createdAt(Instant.now())
                .build();
        album = albumRepository.save(album);
        return AlbumDto.toDto(album);
    }

    @Transactional
    public SongDto addSongToAlbum(Long albumId, Long songId, String userEmail) {
        Album album = findById(albumId).orElseThrow(
                () -> new AlbumNotFoundException(albumId));

        // jeżeli requestujący user nie jest autorem albumu - throw
        if (userEmail == null || !appUserService.getUserByEmail(userEmail).getId()
                .equals(album.getAuthor().getId()))
            throw new AccessDeniedException("Brak uprawnień");

        Song song = songCoreService.getSongById(songId);

        album.getSongs().add(song);
        song.setAlbum(album);

        return SongDto.toDto(songRepository.save(song));
    }

    @Transactional
    public void removeSongFromAlbum(Long albumId, Long songId, String userEmail) {
        Album album = findById(albumId).orElseThrow(
                () -> new AlbumNotFoundException(albumId));

        // jeżeli requestujący user nie jest autorem albumu - throw
        if (userEmail == null || !appUserService.getUserByEmail(userEmail).getId()
                .equals(album.getAuthor().getId()))
            throw new AccessDeniedException("Brak uprawnień");

        Song song = songCoreService.getSongById(songId);

        song.setAlbum(null);
        album.getSongs().remove(song);
        songRepository.save(song);
    }

    @Transactional
    public void deleteAlbum(Long albumId, String userEmail) {
        Album album = findById(albumId).orElseThrow(
                () -> new AlbumNotFoundException(albumId));

        // jeżeli requestujący user nie jest autorem albumu - throw
        if (userEmail == null ||  !appUserService.getUserByEmail(userEmail).getId()
                .equals(album.getAuthor().getId()))
            throw new AccessDeniedException("Brak uprawnień");

        songRepository.unsetAlbumForAlbumId(albumId);
        albumRepository.delete(album);
    }

    public List<AlbumDto> getAlbumsByGenre(String genreName) {
        try {
            Genre genre = Genre.valueOf(genreName.toUpperCase().trim());

            return albumRepository.findAllByGenre(genre)
                    .stream()
                    .map(AlbumDto::toDto)
                    .toList();
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

}
