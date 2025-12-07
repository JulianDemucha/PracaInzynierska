import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { usePlayer } from '../context/PlayerContext.js';
import { useAuth } from '../context/useAuth.js';
import {
    getSongById,
    deleteSong,
    likeSong,
    dislikeSong,
    removeLike,
    removeDislike,
    addSongToFavorites,
    removeSongFromFavorites
} from '../services/songService.js';
import { getImageUrl } from '../services/imageService.js';
import AddToPlaylistModal from '../components/playlist/AddToPlaylistModal.jsx';
import EditSongModal from '../components/song/EditSongModal.jsx';

import defaultCover from '../assets/images/default-avatar.png';
import editIcon from '../assets/images/edit.png';
import playIcon from '../assets/images/play.png';
import pauseIcon from '../assets/images/pause.png';
import heartIconOff from '../assets/images/favorites.png';
import heartIconOn from '../assets/images/favoritesOn.png';
import queueIcon from '../assets/images/addToQueue.png';
import queueIconOn from '../assets/images/addToQueueOn.png';
import likeIcon from '../assets/images/like.png';
import likeIconOn from '../assets/images/likeOn.png';
import dislikeIcon from '../assets/images/disLike.png';
import dislikeIconOn from '../assets/images/disLikeOn.png';
import binIcon from '../assets/images/bin.png';
import plusIcon from '../assets/images/plus.png';

import './SongPage.css';

function SongPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { currentUser } = useAuth();

    const [song, setSong] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isQueuedAnim, setIsQueuedAnim] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [isPlaylistModalOpen, setIsPlaylistModalOpen] = useState(false);

    const {
        currentSong,
        isPlaying,
        playSong,
        pause,
        addToQueue,
        favorites,
        toggleFavorite,
        ratings,
        rateSong,
        viewUpdateTrigger
    } = usePlayer();

    useEffect(() => {
        const fetchSongDetails = async () => {
            if (!song) setLoading(true);

            try {
                const data = await getSongById(id);

                const mappedSong = {
                    ...data,
                    artist: { id: data.authorId, name: data.authorUsername },
                    coverArtUrl: getImageUrl(data.coverStorageKeyId),
                    comments: []
                };

                setSong(mappedSong);
            } catch (err) {
                console.error("Błąd pobierania piosenki:", err);
                if (!song) setError("Nie udało się pobrać szczegółów utworu.");
            } finally {
                setLoading(false);
            }
        };

        fetchSongDetails();
    }, [id, viewUpdateTrigger]);

    const handleSongUpdated = async () => {
        try {
            const data = await getSongById(id);
            const mappedSong = {
                ...data,
                artist: { id: data.authorId, name: data.authorUsername },
                coverArtUrl: getImageUrl(data.coverStorageKeyId),
            };
            setSong(mappedSong);
        } catch (err) {
            console.error("Błąd odświeżania po edycji:", err);
        }
    };

    const isThisSongActive = currentSong?.id === song?.id;
    const isThisSongPlaying = isThisSongActive && isPlaying;

    const handlePlayPause = () => {
        if (isThisSongPlaying) {
            pause();
        } else {
            playSong(song);
        }
    };

    const handleDeleteClick = () => {
        setIsDeleteModalOpen(true);
    };

    const confirmDelete = async () => {
        setIsDeleting(true);
        try {
            await deleteSong(song.id);
            navigate('/profile');
        } catch (err) {
            console.error("Błąd usuwania:", err);
            alert("Nie udało się usunąć utworu.");
            setIsDeleting(false);
            setIsDeleteModalOpen(false);
        }
    };

    const handleAddToQueue = () => {
        if (isQueuedAnim) return;
        addToQueue(song);
        setIsQueuedAnim(true);
        setTimeout(() => setIsQueuedAnim(false), 1500);
    };

    const isOwner = currentUser && song && currentUser.id === song.artist.id;
    const isFavorite = !!favorites[song?.id];
    const currentRating = ratings[song?.id];

    const handleFavoriteClick = async () => {
        toggleFavorite(song.id);

        try {
            if (isFavorite) {
                await removeSongFromFavorites(song.id);
            } else {
                await addSongToFavorites(song.id);
            }
        } catch (error) {
            console.error("Błąd aktualizacji ulubionych:", error);
            toggleFavorite(song.id);
        }
    };

    const handleRatingClick = async (type) => {
        rateSong(song.id, type);

        try {
            if (type === 'like') {
                if (currentRating === 'like') {
                    await removeLike(song.id);
                } else {
                    await likeSong(song.id);
                }
            } else if (type === 'dislike') {
                if (currentRating === 'dislike') {
                    await removeDislike(song.id);
                } else {
                    await dislikeSong(song.id);
                }
            }
        } catch (error) {
            console.error("Błąd aktualizacji oceny:", error);
        }
    };

    if (loading && !song) {
        return (
            <div className="song-page song-loading-container">
                <h2>Ładowanie utworu...</h2>
            </div>
        );
    }

    if (error || !song) {
        return (
            <div className="song-page">
                <h2>404 - Błąd</h2>
                <p>{error || "Nie znaleziono piosenki."}</p>
                <Link to="/" className="back-link">Wróć do strony głównej</Link>
            </div>
        );
    }

    return (
        <div className="song-page">
            <header className="song-header">
                <img
                    src={song.coverArtUrl}
                    alt={song.title}
                    className="song-cover-art"
                    onError={(e) => { e.target.src = defaultCover }}
                />
                <div className="song-details">
                    <span className="song-type">Utwór</span>
                    <h1>{song.title}</h1>
                    <div className="song-meta">
                        <Link to={`/artist/${song.artist.id}`} className="song-artist">{song.artist.name}</Link>
                        <span>•</span>
                        <span className="song-date">{new Date(song.createdAt).getFullYear()}</span>
                        <span>•</span>
                        <span className="song-views">{song.viewCount || 0} wyświetleń</span>
                    </div>
                    <div className="genre-tags">
                        {song.genres && song.genres.map(genre => (
                            <Link
                                key={genre}
                                to={`/genre/${genre}`}
                                className="genre-pill"
                            >
                                {genre}
                            </Link>
                        ))}
                    </div>
                    {!song.publiclyVisible && (
                        <div className="private-badge-wrapper">
                            <span className="private-badge">Prywatny</span>
                        </div>
                    )}
                </div>
            </header>

            <section className="song-controls">
                <div className="controls-group-main">
                    <button className="song-play-button" onClick={handlePlayPause}>
                        <img src={isThisSongPlaying ? pauseIcon : playIcon} alt={isThisSongPlaying ? "Pauza" : "Odtwórz"} />
                    </button>

                    <div className="main-actions-wrapper">
                        <button className={`song-control-button ${isFavorite ? 'active' : ''}`} onClick={handleFavoriteClick}>
                            <img src={isFavorite ? heartIconOn : heartIconOff} alt="Ulubione" />
                        </button>

                        <div className="song-rating">
                            <button
                                className={`song-rating-button ${currentRating === 'like' ? 'active' : ''}`}
                                onClick={() => handleRatingClick('like')}
                            >
                                <img src={currentRating === 'like' ? likeIconOn : likeIcon} alt="Podoba mi się" />
                            </button>
                            <button
                                className={`song-rating-button ${currentRating === 'dislike' ? 'active' : ''}`}
                                onClick={() => handleRatingClick('dislike')}
                            >
                                <img src={currentRating === 'dislike' ? dislikeIconOn : dislikeIcon} alt="Nie podoba mi się" />
                            </button>
                        </div>
                    </div>
                </div>

                <div className="controls-group-secondary">
                    <button className={`song-control-button ${isQueuedAnim ? 'active' : ''}`} onClick={handleAddToQueue}>
                        <img src={isQueuedAnim ? queueIconOn : queueIcon} alt="Dodaj do kolejki" />
                    </button>

                    <button className="song-control-button" onClick={() => setIsPlaylistModalOpen(true)} title="Dodaj do playlisty" >
                        <img src={plusIcon} alt="Dodaj do playlisty" />
                    </button>

                    {isOwner && (
                        <div className="owner-actions">
                            <button
                                className="song-control-button icon-btn"
                                onClick={() => setIsEditModalOpen(true)}
                                title="Edytuj utwór"
                            >
                                <img src={editIcon} alt="Edytuj" className="edit-icon-img" />
                            </button>

                            <button className="delete-song-button icon-btn" onClick={handleDeleteClick} title="Usuń utwór">
                                <img src={binIcon} alt="Usuń" />
                            </button>
                        </div>
                    )}
                </div>
            </section>

            {isDeleteModalOpen && (
                <div className="delete-modal-backdrop" onClick={() => setIsDeleteModalOpen(false)}>
                    <div className="delete-modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Czy na pewno chcesz usunąć ten utwór?</h3>
                        <p>Tej operacji nie można cofnąć.</p>
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
                                {isDeleting ? "Usuwanie..." : "Usuń bezpowrotnie"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <AddToPlaylistModal
                isOpen={isPlaylistModalOpen}
                onClose={() => setIsPlaylistModalOpen(false)}
                songToAdd={song}
            />

            <EditSongModal
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)}
                songToEdit={song}
                onSongUpdated={handleSongUpdated}
            />
        </div>
    );
}

export default SongPage;