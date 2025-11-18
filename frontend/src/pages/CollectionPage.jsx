import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import './CollectionPage.css'; // Dedykowany plik CSS, który zaraz stworzymy
import { usePlayer } from '../context/PlayerContext'; // Importujemy "Mózg" odtwarzacza

// --- Importy ikon ---
import defaultCover from '../assets/images/default-avatar.png';
import playIcon from '../assets/images/play.png';
import pauseIcon from '../assets/images/pause.png';
import heartIconOff from '../assets/images/favorites.png';
import heartIconOn from '../assets/images/favoritesOn.png';
// import likeIcon from '../assets/images/like.png';
// import likeIconOn from '../assets/images/likeOn.png';
import ContextMenu from '../components/common/ContextMenu.jsx'; // Nasze 3 kropki

// --- Przykładowe Dane (Mock Data) ---
// W przyszłości pobierzesz to z backendu używając `id` z useParams
const mockDatabase = {
    album: {
        id: "a1",
        type: "Album",
        title: "Wiped Out!",
        artist: { id: "art1", name: "The Neighbourhood" },
        year: 2015,
        coverArtUrl: "https://i.scdn.co/image/ab67616d0000b2734491007be3379d750c1b48f9",
        songs: [
            { id: "s1", title: "Prey", duration: "4:45" },
            { id: "s2", title: "Cry Baby", duration: "4:18" },
            { id: "s3", title: "R.I.P. 2 My Youth", duration: "3:49" },
        ]
    },
    playlist: {
        id: "p1",
        type: "Playlista",
        title: "Moja playlista nr 1",
        artist: { id: "u1", name: "Hubert" }, // Właściciel playlisty
        year: 2024,
        coverArtUrl: "https://placehold.co/300x300/1DB954/white?text=Playlista",
        songs: [
            { id: "s1", title: "Prey", artist: { name: "The Neighbourhood" }, duration: "4:45" },
            { id: "s3", title: "R.I.P. 2 My Youth", artist: { name: "The Neighbourhood" }, duration: "3:49" },
            { id: "s4", title: "Inny utwór", artist: { name: "Inny Artysta" }, duration: "2:30" },
        ]
    }
};
// ---------------------------------

function CollectionPage() {
    // Odczytuje ID z adresu URL (np. /album/a1)
    const { id } = useParams();

    // Na razie pobieramy dane z 'mockDatabase'
    // W przyszłości tu będzie 'fetch' do API
    // (Ta logika jest uproszczona, zakłada, że wiemy, czy to album, czy playlista)
    const collection = mockDatabase.album; // Możesz zmienić na 'playlist' do testów

    // --- Stany Lokalne ---
    const [isFavorite, setIsFavorite] = useState(false); // Serduszko dla całego albumu
    // Stan dla polubień *poszczególnych* piosenek na liście
    const [songLikes, setSongLikes] = useState({});

    // --- Stany Globalne z "Mózgu" ---
    const { currentSong, isPlaying, playSong, pause } = usePlayer();

    // Funkcja do odtwarzania całego albumu/playlisty (zaczyna od pierwszej piosenki)
    const handlePlayCollection = () => {
        // Sprawdź, czy obecna piosenka jest z tej kolekcji
        const isThisCollectionPlaying = collection.songs.some(s => s.id === currentSong?.id) && isPlaying;

        if (isThisCollectionPlaying) {
            pause(); // Jeśli to gra, zapauzuj
        } else {
            // Zacznij grać pierwszą piosenkę z listy
            playSong(collection.songs[0]);
            // W przyszłości: dodaj resztę do kolejki
        }
    };

    // Funkcja do polubienia piosenki na liście
    const handleLikeSong = (songId) => {
        setSongLikes(prev => ({
            ...prev,
            [songId]: !prev[songId] // Odwraca stan (true/false) dla tej jednej piosenki
        }));
    };

    // Opcje dla menu "3 kropki"
    const collectionMenuOptions = [
        { label: "Dodaj do kolejki", onClick: () => console.log("Dodaj do kolejki") },
        { label: "Zapisz w swojej bibliotece", onClick: () => console.log("Zapisano") },
        { label: "Udostępnij", onClick: () => console.log("Udostępniono") }
    ];

    // Sprawdza, czy cały album/playlista jest aktywnie odtwarzana
    const isThisCollectionPlaying = collection.songs.some(s => s.id === currentSong?.id) && isPlaying;

    return (
        <div className="collection-page">
            {/* ===== 1. NAGŁÓWEK (Ten sam styl co SongPage) ===== */}
            <header className="song-header">
                <img src={collection.coverArtUrl || defaultCover} alt={collection.title} className="song-cover-art" />
                <div className="song-details">
                    <span className="song-type">{collection.type}</span>
                    <h1>{collection.title}</h1>
                    <div className="song-meta">
                        <Link to={`/artist/${collection.artist.id}`} className="song-artist">{collection.artist.name}</Link>
                        <span>•</span>
                        <span>{collection.year}</span>
                        <span>•</span>
                        <span className="song-duration">{collection.songs.length} utworów</span>
                    </div>
                </div>
            </header>

            {/* ===== 2. KONTROLKI ===== */}
            <section className="song-controls">
                <button className="song-play-button" onClick={handlePlayCollection}>
                    <img src={isThisCollectionPlaying ? pauseIcon : playIcon} alt={isThisCollectionPlaying ? "Pauza" : "Odtwórz"} />
                </button>
                <button className={`song-control-button ${isFavorite ? 'active' : ''}`} onClick={() => setIsFavorite(!isFavorite)}>
                    <img src={isFavorite ? heartIconOn : heartIconOff} alt="Ulubione" />
                </button>
                <ContextMenu options={collectionMenuOptions} />
            </section>

            {/* ===== 3. LISTA UTWORÓW ===== */}
            <section className="song-list-container">
                {/* Nagłówek listy (Tabela) */}
                <div className="song-list-header">
                    <span className="song-header-track">#</span>
                    <span className="song-header-title">TYTUŁ</span>
                    <span className="song-header-duration">CZAS</span>
                    <span className="song-header-like"></span> {/* Puste miejsce na serduszko */}
                </div>

                {/* Lista (mapowanie po piosenkach) */}
                <ul className="song-list">
                    {collection.songs.map((song, index) => {
                        const isThisSongPlaying = currentSong?.id === song.id && isPlaying;
                        const isSongLiked = songLikes[song.id] || false;

                        return (
                            <li
                                key={song.id}
                                className={`song-list-item ${currentSong?.id === song.id ? 'active' : ''}`}
                                // Podwójne kliknięcie odtwarza piosenkę
                                onDoubleClick={() => playSong(song)}
                            >
                                <span className="song-track-number">
                                    {/* Pokaż ikonę pauzy/play LUB numer */}
                                    {isThisSongPlaying ? (
                                        <img src={pauseIcon} alt="Pauza" onClick={() => pause()}/>
                                    ) : (
                                        <img src={playIcon} alt="Odtwórz" onClick={() => playSong(song)}/>
                                    )}
                                    <span className="track-number">{index + 1}</span>
                                </span>
                                <div className="song-item-details">
                                    <span className="song-item-title">{song.title}</span>
                                    {/* Jeśli to playlista, pokaż artystę przy każdej piosence */}
                                    {collection.type === "Playlista" && (
                                        <Link to={`/artist/${song.artist.id}`} className="song-item-artist">{song.artist.name}</Link>
                                    )}
                                </div>
                                <span className="song-item-duration">{song.duration}</span>
                                <button
                                    className={`song-item-like-button ${isSongLiked ? 'active' : ''}`}
                                    onClick={() => handleLikeSong(song.id)}
                                >
                                    <img src={isSongLiked ? heartIconOn : heartIconOff} alt="Polub" />
                                </button>
                            </li>
                        );
                    })}
                </ul>
            </section>
        </div>
    );
}

export default CollectionPage;