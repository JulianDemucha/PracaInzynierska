import React, { useState, useEffect, useMemo } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import './CollectionPage.css';
import { usePlayer } from '../context/PlayerContext.js';
import { useAuth } from '../context/useAuth.js';
import {
    getAlbumById,
    getSongsByAlbumId,
    deleteAlbum
} from '../services/albumService.js';
import { getImageUrl } from '../services/imageService.js';

import ContextMenu from '../components/common/ContextMenu.jsx';

import binIcon from '../assets/images/bin.png';
import defaultCover from '../assets/images/default-avatar.png';
import playIcon from '../assets/images/play.png';
import pauseIcon from '../assets/images/pause.png';
import heartIconOff from '../assets/images/favorites.png';
import heartIconOn from '../assets/images/favoritesOn.png';
import likeIcon from '../assets/images/like.png';
import likeIconOn from '../assets/images/likeOn.png';
import dislikeIcon from '../assets/images/disLike.png';
import dislikeIconOn from '../assets/images/disLikeOn.png';

// Pomocnicza funkcja formatowania czasu
function formatTime(seconds) {
    if (!seconds || isNaN(seconds)) return "0:00";
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
}

function CollectionPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { currentUser } = useAuth();

    // --- STANY ---
    const [collection, setCollection] = useState(null); // Dane albumu
    const [songs, setSongs] = useState([]);             // Lista piosenek
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [isAlbumFavorite, setIsAlbumFavorite] = useState(false);

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    // Globalny Player
    const {
        currentSong, isPlaying, playSong, pause, addToQueue,
        favorites, toggleFavorite, ratings, rateSong
    } = usePlayer();

    // --- POBIERANIE DANYCH ---
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);

                // Pobieramy równolegle dane albumu i listę piosenek
                const [albumData, songsData] = await Promise.all([
                    getAlbumById(id),
                    getSongsByAlbumId(id)
                ]);

                // Mapujemy piosenki
                const mappedSongs = songsData.map(s => ({
                    ...s,
                    artist: { id: s.authorId, name: s.authorUsername || "Nieznany" },
                    coverArtUrl: getImageUrl(albumData.coverStorageKeyId),
                    duration: s.duration || 0
                }));

                setCollection(albumData);
                setSongs(mappedSongs);

            } catch (err) {
                console.error("Błąd pobierania albumu:", err);
                setError("Nie udało się załadować albumu.");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [id]);

    // OBLICZANIE UNIKALNYCH GATUNKÓW Z PIOSENEK
    const albumGenres = useMemo(() => {
        if (!songs || songs.length === 0) return [];
        const allGenres = songs.flatMap(song => song.genres || []);
        return [...new Set(allGenres)];
    }, [songs]);

    const handleDeleteClick = () => setIsDeleteModalOpen(true);

    const confirmDelete = async () => {
        setIsDeleting(true);
        try {
            await deleteAlbum(collection.id);
            navigate('/profile');
        } catch (err) {
            console.error("Błąd usuwania albumu:", err);
            alert("Nie udało się usunąć albumu.");
            setIsDeleting(false);
            setIsDeleteModalOpen(false);
        }
    };

    const isOwner = currentUser && collection && currentUser.id === collection.authorId;

    if (loading) return <div className="collection-page" style={{padding:'20px'}}>Ładowanie albumu...</div>;
    if (error || !collection) return <div className="collection-page" style={{padding:'20px'}}>{error || "Album nie istnieje."}</div>;

    // --- LOGIKA ODTWARZANIA ---

    const handlePlayCollection = () => {
        if (songs.length === 0) return;
        const firstSong = songs[0];
        if (currentSong?.id === firstSong.id) {
            if (isPlaying) pause(); else playSong(firstSong);
        } else {
            playSong(firstSong, songs);
        }
    };

    const handlePlayTrack = (song) => {
        if (currentSong?.id === song.id) {
            if (isPlaying) pause(); else playSong(song);
        } else {
            playSong(song, songs);
        }
    };

    const albumMenuOptions = [
        { label: "Dodaj wszystkie do kolejki", onClick: () => songs.forEach(s => addToQueue(s)) },
    ];

    const isAnySongFromCollectionPlaying = songs.some(s => s.id === currentSong?.id);
    const showPauseOnHeader = isAnySongFromCollectionPlaying && isPlaying;

    return (
        <div className="collection-page">
            {/* --- NAGŁÓWEK --- */}
            <header className="song-header">
                <img
                    src={getImageUrl(collection.coverStorageKeyId)}
                    alt={collection.title}
                    className="song-cover-art"
                    onError={(e) => {e.target.src = defaultCover}}
                />
                <div className="song-details">
                    <span className="song-type">ALBUM</span>
                    <h1>{collection.title}</h1>
                    <div className="song-meta">
                        <Link to={`/artist/${collection.authorId}`} className="song-artist">
                            {collection.authorName}
                        </Link>
                        <span>•</span>
                        <span>{new Date(collection.createdAt).getFullYear()}</span>
                        <span>•</span>
                        <span className="song-duration">{songs.length} utworów</span>
                    </div>

                    <div className="genre-tags">
                        {albumGenres.length > 0 ? (
                            albumGenres.map(genre => (
                                <Link
                                    key={genre}
                                    to={`/genre/${genre}`}
                                    className="genre-pill"
                                >
                                    {genre}
                                </Link>
                            ))
                        ) : (
                            collection.description && (
                                <p style={{color:'#aaa', fontSize:'0.9rem', margin: 0}}>{collection.description}</p>
                            )
                        )}
                    </div>

                    {/* --- NOWOŚĆ: Kafelek "Prywatny" (taki sam jak w SongPage) --- */}
                    {!collection.publiclyVisible && (
                        <div style={{ marginTop: '10px' }}>
                            <span style={{
                                border: '1px solid #666',
                                padding: '2px 6px',
                                borderRadius: '4px',
                                fontSize: '0.7rem',
                                color: '#aaa',
                                textTransform: 'uppercase'
                            }}>
                                Prywatny
                            </span>
                        </div>
                    )}

                </div>
            </header>

            {/* --- KONTROLKI --- */}
            <section className="song-controls">
                <button className="song-play-button" onClick={handlePlayCollection}>
                    <img src={showPauseOnHeader ? pauseIcon : playIcon} alt={showPauseOnHeader ? "Pauza" : "Odtwórz"} />
                </button>
                <button className={`song-control-button ${isAlbumFavorite ? 'active' : ''}`} onClick={() => setIsAlbumFavorite(!isAlbumFavorite)}>
                    <img src={isAlbumFavorite ? heartIconOn : heartIconOff} alt="Ulubione" />
                </button>
                {isOwner && (
                    <button className="delete-song-button icon-btn" onClick={handleDeleteClick} title="Usuń album">
                        <img src={binIcon} alt="Usuń" />
                    </button>
                )}
                <ContextMenu options={albumMenuOptions} />
            </section>

            {/* --- LISTA PIOSENEK --- */}
            <section className="song-list-container">
                <div className="song-list-header">
                    <span className="song-header-track">#</span>
                    <span className="song-header-title">TYTUŁ</span>
                    <span className="song-header-actions"></span>
                    <span className="song-header-duration">CZAS</span>
                    <span style={{width:'40px'}}></span>
                </div>

                <ul className="song-list">
                    {songs.map((song, index) => {
                        const isThisSongActive = currentSong?.id === song.id;
                        const isThisSongPlaying = isThisSongActive && isPlaying;
                        const isSongLiked = !!favorites[song.id];
                        const songRating = ratings[song.id];

                        const songMenuOptions = [
                            { label: isSongLiked ? "Usuń z polubionych" : "Dodaj do polubionych", onClick: () => toggleFavorite(song.id) },
                            { label: "Dodaj do kolejki", onClick: () => addToQueue(song) },
                            { label: "Przejdź do artysty", onClick: () => navigate(`/artist/${song.artist.id}`) }
                        ];

                        return (
                            <li key={song.id} className={`song-list-item ${isThisSongActive ? 'active' : ''}`} onDoubleClick={() => handlePlayTrack(song)}>
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

                                <div className="song-item-details">
                                    <span className={`song-item-title ${isThisSongActive ? 'highlight' : ''}`}>{song.title}</span>
                                    <Link to={`/artist/${song.artist.id}`} className="song-item-artist" onClick={(e) => e.stopPropagation()}>
                                        {song.artist.name}
                                    </Link>
                                </div>

                                <div className="song-item-actions">
                                    <button className={`action-btn ${isSongLiked ? 'active' : ''}`} onClick={() => toggleFavorite(song.id)}>
                                        <img src={isSongLiked ? heartIconOn : heartIconOff} alt="Like" />
                                    </button>
                                    <button className={`action-btn ${songRating === 'like' ? 'active' : ''}`} onClick={() => rateSong(song.id, 'like')}>
                                        <img src={songRating === 'like' ? likeIconOn : likeIcon} alt="Up" />
                                    </button>
                                    <button className={`action-btn ${songRating === 'dislike' ? 'active' : ''}`} onClick={() => rateSong(song.id, 'dislike')}>
                                        <img src={songRating === 'dislike' ? dislikeIconOn : dislikeIcon} alt="Down" />
                                    </button>
                                </div>

                                <span className="song-item-duration">{formatTime(song.duration)}</span>
                                <div className="song-context-menu-wrapper">
                                    <ContextMenu options={songMenuOptions} />
                                </div>
                            </li>
                        );
                    })}
                </ul>
            </section>
            {isDeleteModalOpen && (
                <div className="delete-modal-backdrop" onClick={() => setIsDeleteModalOpen(false)}>
                    <div className="delete-modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Usunąć album "{collection?.title}"?</h3>
                        <p style={{color: '#ff4444'}}>
                            Uwaga: Usunięcie albumu spowoduje bezpowrotne usunięcie wszystkich {songs.length} piosenek, które się w nim znajdują!
                        </p>
                        <div className="delete-modal-actions">
                            <button
                                className="cancel-btn"
                                onClick={() => setIsDeleteModalOpen(false)}
                                disabled={isDeleting}
                            >
                                Anuluj
                            </button>
                            <button
                                className="confirm-delete-btn"
                                onClick={confirmDelete}
                                disabled={isDeleting}
                            >
                                {isDeleting ? "Usuwanie..." : "Usuń wszystko"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default CollectionPage;