package com.soundspace.service;

import com.soundspace.entity.Playlist;
import com.soundspace.entity.PlaylistSong;
import com.soundspace.entity.Song;
import com.soundspace.repository.PlaylistRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/*
    serwis to takie sigmastyczne miejsce w ktorym piszemy cala logike aplikacji dotyczaca
    danej encji ogolnie jak tu np mamy playliste to pyk jakies dodanie do playlisty, dodanie like
    usuniecie z playlisty zmiana pozycji jakiejs piosenki w playliscie itp.
    nie napiszemy wszystkich potrzebnych metod do kazdego serwisu na raz bo to bedzie gdziestam
    dochodzic w miedzyczasie, ale te najoczywistsze mozemy
 */
@Service
@AllArgsConstructor
public class PlaylistService {
    /*
        tu robimy dependency injection, obejrzyj sobie cos na ten temat bo to
        jedna z najwazniejszych rzeczy w springbootcie jak nie najwazniejsza.
        ciezki temat na poczatku do realnego zrozumienia tak dobitnie ale jak
        cokolwiek ogarniesz to sigma
     */
    private final PlaylistRepository playlistRepository;

    /*
        pozniej do frontu nie bedziemy wysylac plikow-encji, tylko data objecty na nie zrobimy,
        ale to ci pokaze. pozniej po prostu zmapujemy to na jakis PlaylistDto przed zwroceniem.
        to i tak tak jako przyklad
    */
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAllByOrderByIdAsc();
    }

    @Transactional /* to taki trik co ci go wytlumacze ale ogolnie zapisujac tu songa
    do playlisty zapisujemy go w bazie (i tu sie wykona insert w bazie) ale pozniej
    jeszcze updatujemy updatedAt na playliscie a to druga oddzielna tabela
    wiec najpeirw japidi robi sie insert nowego playlistsonga a pozniej update na istniejacej juz
    playlistcie czyi 2 operacje (1,2. 1+1 = 2 albo == jak jestes programista albo === jak cw z javascripta)
    no i ten transactional w skrocie robi cos takiego ze jakby w jednym poleceniu robi i ten insert i update
    dzieki temu jakby cos sie wywalilo w update playlist to insert tez nie przejdzie. tu az takiej katastrofy
    by nie bylo bo po prostu song by sie dodal do playlisty, ale playlista nie miala by tylko nowej daty aktualizacji
    ale tak ogolnie to ratuje dupe to
    */
    public void addSongToPlaylist(Playlist playlist, Song song) {

        //tu se sprawdzam w lambdzie czy juz takiej smisznej piosnki nima w playlistcie
        boolean alreadyExists = playlist.getPlaylistSongs().stream()
                .anyMatch(playlistSong -> playlistSong.getSong().equals(song));

        if (alreadyExists) {
            // throw new IllegalArgumentException("blablablabla hubercica to piwnica");
            // todo to na pozniej sie przyda nie bede sie bawic tera
            return;
        }

        int nextPosition = playlist.getPlaylistSongs().size();

        // se robie playlistsonga z ustawiona playlista i piosenka do ktorej nawiazuje cn
        PlaylistSong newEntry = new PlaylistSong(playlist, song, nextPosition);

        // getplaylistsongs daje mi dostep do listy piosenek i pyk .add se dodaje cn
        playlist.getPlaylistSongs().add(newEntry);

        playlist.setUpdatedAt(Instant.now());

        // bo i zapisujem w bazie
        playlistRepository.save(playlist);
    }
    // powinno dzialac
    // a i komentarze se mozesz usunac jak czaisz baze
}
