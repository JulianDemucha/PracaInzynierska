import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import './CollectionPage.css';
import { usePlayer } from '../context/PlayerContext.js';

import defaultCover from '../assets/images/default-avatar.png';
import playIcon from '../assets/images/play.png';
import pauseIcon from '../assets/images/pause.png';
import heartIconOff from '../assets/images/favorites.png';
import heartIconOn from '../assets/images/favoritesOn.png';
import likeIcon from '../assets/images/like.png';
import likeIconOn from '../assets/images/likeOn.png';
import dislikeIcon from '../assets/images/disLike.png';
import dislikeIconOn from '../assets/images/disLikeOn.png';

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
            { id: "s1", title: "Prey", duration: "4:45", artist: { id: "art1", name: "The Neighbourhood" } },
            { id: "s2", title: "Cry Baby", duration: "4:18", artist: { id: "art1", name: "The Neighbourhood" } },
            { id: "s3", title: "R.I.P. 2 My Youth", duration: "3:49", artist: { id: "art1", name: "The Neighbourhood" } },
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
            { id: "s1", title: "Prey", artist: { id: "art1", name: "The Neighbourhood" }, duration: "4:45" },
            { id: "s3", title: "R.I.P. 2 My Youth", artist: { id: "art1", name: "The Neighbourhood" }, duration: "3:49" },
            { id: "s4", title: "Inny utwór", artist: { id: "art2", name: "Inny Artysta" }, duration: "2:30" },
        ]
    }
};

function CollectionPage() {
    const { id } = useParams();
    const navigate = useNavigate(); // Hook do nawigacji (przejście do artysty)

    // Stan lokalny dla polubienia CAŁEGO albumu (nagłówek)
    const [isAlbumFavorite, setIsAlbumFavorite] = useState(false);

    // Pobieramy funkcje z Globalnego Contextu
    const {
        currentSong,
        isPlaying,
        playSong,
        pause,
        addToQueue,
        favorites,
        toggleFavorite,
        ratings,
        rateSong
    } = usePlayer();

    // --- Wybór danych ---
    let collection = null;
    if (mockDatabase.album.id === id) {
        collection = mockDatabase.album;
    } else if (mockDatabase.playlist.id === id) {
        collection = mockDatabase.playlist;
    }

    if (!collection) {
        return <div style={{padding: '20px', color: 'white'}}>Nie znaleziono kolekcji.</div>;
    }

    // Obsługa przycisku Play Albumu
    const handlePlayCollection = () => {
        const firstSong = collection.songs[0];
        if (currentSong?.id === firstSong.id) {
            if (isPlaying) pause();
            else playSong(firstSong);
        } else {
            playSong(firstSong, collection.songs);
        }
    };

    // Obsługa kliknięcia w wiersz (Play Track)
    const handlePlayTrack = (song) => {
        if (currentSong?.id === song.id) {
            if (isPlaying) pause();
            else playSong(song);
        } else {
            playSong(song, collection.songs);
        }
    };

    // Menu dla całego albumu
    const albumMenuOptions = [
        { label: "Dodaj wszystkie do kolejki", onClick: () => collection.songs.forEach(s => addToQueue(s)) },
    ];

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
                        {/* Używamy linku, ale bezpiecznie sprawdzamy czy artist istnieje */}
                        <Link to={`/artist/${collection.artist.id}`} className="song-artist">{collection.artist.name}</Link>
                        <span>•</span>
                        <span>{collection.year}</span>
                        <span>•</span>
                        <span className="song-duration">{collection.songs.length} utworów</span>
                    </div>
                </div>
            </header>

            {/* --- KONTROLKI GŁÓWNE --- */}
            <section className="song-controls">
                <button className="song-play-button" onClick={handlePlayCollection}>
                    <img src={showPauseOnHeader ? pauseIcon : playIcon} alt={showPauseOnHeader ? "Pauza" : "Odtwórz"} />
                </button>
                <button className={`song-control-button ${isAlbumFavorite ? 'active' : ''}`} onClick={() => setIsAlbumFavorite(!isAlbumFavorite)}>
                    <img src={isAlbumFavorite ? heartIconOn : heartIconOff} alt="Ulubione" />
                </button>
                <ContextMenu options={albumMenuOptions} />
            </section>

            {/* --- LISTA PIOSENEK --- */}
            <section className="song-list-container">
                <div className="song-list-header">
                    <span className="song-header-track">#</span>
                    <span className="song-header-title">TYTUŁ</span>
                    <span className="song-header-actions"></span>
                    <span className="song-header-duration">CZAS</span>
                </div>

                <ul className="song-list">
                    {collection.songs.map((song, index) => {
                        const isThisSongActive = currentSong?.id === song.id;
                        const isThisSongPlaying = isThisSongActive && isPlaying;

                        // Pobieramy stany z Contextu
                        const isSongLiked = !!favorites[song.id];
                        const songRating = ratings[song.id];

                        // --- OPCJE MENU KONTEKSTOWEGO DLA TEJ PIOSENKI ---
                        const songMenuOptions = [
                            {
                                label: isSongLiked ? "Usuń z polubionych" : "Dodaj do polubionych",
                                onClick: () => toggleFavorite(song.id)
                            },
                            {
                                label: "Dodaj do kolejki",
                                onClick: () => addToQueue(song)
                            },
                            {
                                label: "Przejdź do artysty",
                                onClick: () => navigate(`/artist/${song.artist.id}`)
                            }
                        ];

                        return (
                            <li
                                key={song.id}
                                className={`song-list-item ${isThisSongActive ? 'active' : ''}`}
                                onDoubleClick={() => handlePlayTrack(song)}
                            >
                                {/* 1. Numer / Ikona Play */}
                                <span className="song-track-number">
                                    {isThisSongPlaying ? (
                                        <img src={pauseIcon} alt="Pauza" onClick={() => pause()}/>
                                    ) : (
                                        <div className="number-container">
                                            <span className="track-number">{index + 1}</span>
                                            <img src={playIcon} alt="Odtwórz" className="play-icon-hover" onClick={() => handlePlayTrack(song)}/>
                                        </div>
                                    )}
                                </span>

                                {/* 2. Tytuł i Artysta */}
                                <div className="song-item-details">
                                    <span className={`song-item-title ${isThisSongActive ? 'highlight' : ''}`}>{song.title}</span>
                                    <Link
                                        to={`/artist/${song.artist.id}`}
                                        className="song-item-artist"
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        {song.artist.name}
                                    </Link>
                                </div>

                                {/* 3. Sekcja Akcji (Serduszko, Łapki, 3 kropki) */}
                                <div className="song-item-actions">
                                    {/* Serduszko */}
                                    <button className={`action-btn ${isSongLiked ? 'active' : ''}`} onClick={() => toggleFavorite(song.id)}>
                                        <img src={isSongLiked ? heartIconOn : heartIconOff} alt="Like" />
                                    </button>

                                    {/* Łapka w górę */}
                                    <button className={`action-btn ${songRating === 'like' ? 'active' : ''}`} onClick={() => rateSong(song.id, 'like')}>
                                        <img src={songRating === 'like' ? likeIconOn : likeIcon} alt="Thumb Up" />
                                    </button>

                                    {/* Łapka w dół */}
                                    <button className={`action-btn ${songRating === 'dislike' ? 'active' : ''}`} onClick={() => rateSong(song.id, 'dislike')}>
                                        <img src={songRating === 'dislike' ? dislikeIconOn : dislikeIcon} alt="Thumb Down" />
                                    </button>
                                </div>

                                {/* 4. Czas i Menu Kontekstowe */}
                                <span className="song-item-duration">{song.duration}</span>

                                <div className="song-context-menu-wrapper">
                                    <ContextMenu options={songMenuOptions} />
                                </div>
                            </li>
                        );
                    })}
                </ul>
            </section>
        </div>
    );
}

export default CollectionPage;