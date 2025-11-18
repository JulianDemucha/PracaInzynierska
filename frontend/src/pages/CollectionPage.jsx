import React, { useState } from 'react';
import { useParams, Link} from 'react-router-dom';
import './CollectionPage.css';
import { usePlayer } from '../context/PlayerContext';

import defaultCover from '../assets/images/default-avatar.png';
import playIcon from '../assets/images/play.png';
import pauseIcon from '../assets/images/pause.png';
import heartIconOff from '../assets/images/favorites.png';
import heartIconOn from '../assets/images/favoritesOn.png';
import ContextMenu from '../components/common/ContextMenu.jsx';

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
        artist: { id: "u1", name: "Hubert" },
        year: 2024,
        coverArtUrl: "https://placehold.co/300x300/1DB954/white?text=Playlista",
        songs: [
            { id: "s1", title: "Prey", artist: { name: "The Neighbourhood" }, duration: "4:45" },
            { id: "s3", title: "R.I.P. 2 My Youth", artist: { name: "The Neighbourhood" }, duration: "3:49" },
            { id: "s4", title: "Inny utwór", artist: { name: "Inny Artysta" }, duration: "2:30" },
        ]
    }
};

function CollectionPage() {
    const { id } = useParams();

    const [isFavorite, setIsFavorite] = useState(false);
    const [songLikes, setSongLikes] = useState({});
    const { currentSong, isPlaying, playSong, pause } = usePlayer();

    let collection = null;
    if (mockDatabase.album.id === id) {
        collection = mockDatabase.album;
    } else if (mockDatabase.playlist.id === id) {
        collection = mockDatabase.playlist;
    }
    if (!collection) {
        return <div style={{padding: '20px', color: 'white'}}>Nie znaleziono takiej kolekcji.</div>;
    }

    // Funkcja odtwarzania całego albumu
    const handlePlayCollection = () => {
        const firstSong = collection.songs[0];
        // Sprawdzamy czy PIERWSZA piosenka albumu właśnie gra
        if (currentSong?.id === firstSong.id) {
            if (isPlaying) pause();
            else playSong(firstSong);
        } else {
            // Przekazujemy też całą listę, aby ustawić kolejkę (drugi argument)
            playSong(firstSong, collection.songs);
        }
    };

    // Funkcja odtwarzania pojedynczego utworu
    const handlePlayTrack = (song) => {
        if (currentSong?.id === song.id) {
            if (isPlaying) pause();
            else playSong(song);
        } else {
            // Odtwarzamy klikniętą piosenkę i ustawiamy resztę albumu jako kolejkę
            playSong(song, collection.songs);
        }
    };

    const handleLikeSong = (songId) => {
        setSongLikes(prev => ({
            ...prev,
            [songId]: !prev[songId]
        }));
    };

    const collectionMenuOptions = [
        { label: "Dodaj do kolejki", onClick: () => console.log("Dodaj do kolejki") },
        { label: "Zapisz w swojej bibliotece", onClick: () => console.log("Zapisano") },
        { label: "Udostępnij", onClick: () => console.log("Udostępniono") }
    ];

    // Sprawdzamy, czy jakakolwiek piosenka z tego albumu gra
    const isAnySongFromCollectionPlaying = collection.songs.some(s => s.id === currentSong?.id);
    const showPauseOnHeader = isAnySongFromCollectionPlaying && isPlaying;

    return (
        <div className="collection-page">
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

            <section className="song-controls">
                <button className="song-play-button" onClick={handlePlayCollection}>
                    <img src={showPauseOnHeader ? pauseIcon : playIcon} alt={showPauseOnHeader ? "Pauza" : "Odtwórz"} />
                </button>
                <button className={`song-control-button ${isFavorite ? 'active' : ''}`} onClick={() => setIsFavorite(!isFavorite)}>
                    <img src={isFavorite ? heartIconOn : heartIconOff} alt="Ulubione" />
                </button>
                <ContextMenu options={collectionMenuOptions} />
            </section>

            <section className="song-list-container">
                <div className="song-list-header">
                    <span className="song-header-track">#</span>
                    <span className="song-header-title">TYTUŁ</span>
                    <span className="song-header-duration">CZAS</span>
                    <span className="song-header-like"></span>
                </div>

                <ul className="song-list">
                    {collection.songs.map((song, index) => {
                        // Logika wizualna bazująca na GLOBALNYM stanie
                        const isThisSongActive = currentSong?.id === song.id;
                        const isThisSongPlaying = isThisSongActive && isPlaying;
                        const isSongLiked = songLikes[song.id] || false;

                        return (
                            <li
                                key={song.id}
                                className={`song-list-item ${isThisSongActive ? 'active' : ''}`}
                                onDoubleClick={() => handlePlayTrack(song)}
                            >
                                <span className="song-track-number">
                                    {isThisSongPlaying ? (
                                        <img src={pauseIcon} alt="Pauza" onClick={() => pause()}/>
                                    ) : (
                                        <img src={playIcon} alt="Odtwórz" className="play-icon" onClick={() => handlePlayTrack(song)}/>
                                    )}
                                    <span className="track-number">{index + 1}</span>
                                </span>
                                <div className="song-item-details">
                                    <span className="song-item-title">{song.title}</span>
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