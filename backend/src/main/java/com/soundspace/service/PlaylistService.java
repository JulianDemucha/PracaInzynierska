package com.soundspace.service;

import com.soundspace.entity.Playlist;
import com.soundspace.entity.Song;
import com.soundspace.repository.PlaylistRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

    /*
    todo:
        - dodanie playlisty
        - usuniecie playlisty (razem z jej piosenkami) (! uwzglednic position !)
        - dodawanie istniejacej piosenki do playlisty
        - usuwanie piosenki z playlisty
        - zmiana pozycji piosenki na playlistcie
     */

@Service
@AllArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;

    // wip - tu bedzie sie mapowalo w playlistdto i zwracalo jakos
//    public List<PlaylistDto> getAllPlaylists() {
//        return playlistRepository.findAllByOrderByIdAsc();
//    }

    public void addSongToPlaylist(Playlist playlist, Song song) {

    }

}
