import React, { useState, useEffect, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import './SongPage.css';
import { usePlayer } from '../context/PlayerContext.js';
import { getSongById, getCoverUrl } from '../services/songService.js';

import defaultAvatar from '../assets/images/default-avatar.png';
import defaultCover from '../assets/images/default-avatar.png';
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

function SongPage() {
    const { id } = useParams();

    const [song, setSong] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [isQueuedAnim, setIsQueuedAnim] = useState(false);
    const [commentSort, setCommentSort] = useState('popular');
    const [visibleComments, setVisibleComments] = useState(10);
    const [commentLikes, setCommentLikes] = useState({});

    const {
        currentSong,
        isPlaying,
        playSong,
        pause,
        addToQueue,
        favorites, toggleFavorite,
        ratings, rateSong
    } = usePlayer();

    // --- 2. POBIERANIE DANYCH Z BACKENDU ---
    useEffect(() => {
        const fetchSongDetails = async () => {
            try {
                setLoading(true);
                const data = await getSongById(id);

                const mappedSong = {
                    ...data,
                    artist: { id: 0, name: data.authorUsername || data.authorLogin || "Nieznany" },
                    coverArtUrl: getCoverUrl(data.id),
                    duration: "3:00",
                    comments: []
                };

                setSong(mappedSong);
            } catch (err) {
                console.error("Błąd pobierania piosenki:", err);
                setError("Nie udało się pobrać szczegółów utworu.");
            } finally {
                setLoading(false);
            }
        };

        fetchSongDetails();
    }, [id]);

    // --- 3. LOGIKA UI ---

    const sortedComments = useMemo(() => {
        if (!song || !song.comments) return [];
        const sorted = [...song.comments];
        if (commentSort === 'popular') {
            sorted.sort((a, b) => b.likes - a.likes);
        } else {
            sorted.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        }
        return sorted;
    }, [song, commentSort]);

    const isThisSongActive = currentSong?.id === song?.id;
    const isThisSongPlaying = isThisSongActive && isPlaying;

    const handlePlayPause = () => {
        if (isThisSongPlaying) {
            pause();
        } else {
            playSong(song);
        }
    };

    const isFavorite = !!favorites[song?.id];
    const currentRating = ratings[song?.id];

    const handleAddToQueue = () => {
        if (isQueuedAnim) return;
        addToQueue(song);
        setIsQueuedAnim(true);
        setTimeout(() => setIsQueuedAnim(false), 1500);
    };

    const handleCommentLike = (commentId) => {
        setCommentLikes(prev => ({
            ...prev,
            [commentId]: !prev[commentId]
        }));
    };

    // --- 4. EKRANY ŁADOWANIA I BŁĘDU ---

    if (loading) {
        return (
            <div className="song-page" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
                <h2>Ładowanie utworu...</h2>
            </div>
        );
    }

    if (error || !song) {
        return (
            <div className="song-page">
                <h2>404 - Błąd</h2>
                <p>{error || "Nie znaleziono piosenki."}</p>
                <Link to="/" style={{color: '#8A2BE2'}}>Wróć do strony głównej</Link>
            </div>
        );
    }

    // --- 5. WŁAŚCIWY RENDER ---

    return (
        <div className="song-page">
            {/* ===== NAGŁÓWEK ===== */}
            <header className="song-header">
                <img
                    src={song.coverArtUrl}
                    alt={song.title}
                    className="song-cover-art"
                    onError={(e) => {e.target.src = defaultCover}}
                />
                <div className="song-details">
                    <span className="song-type">Singiel</span>
                    <h1>{song.title}</h1>
                    <div className="song-meta">
                        {/* Uwaga: Backend SongDto nie ma ID autora, tylko login. Link może nie działać idealnie, dopóki nie zmienisz routingu na /artist/username */}
                        <Link to={`/artist/${song.artist.name}`} className="song-artist">{song.artist.name}</Link>
                        <span>•</span>
                        <span className="song-duration">{song.duration}</span>
                        <span>•</span>
                        <span className="song-date">{new Date(song.createdAt).getFullYear()}</span>
                    </div>
                    <div className="genre-tags">
                        {song.genres && song.genres.map(genre => (
                            <span key={genre} className="genre-pill">{genre}</span>
                        ))}
                    </div>
                    {/* Status widoczności (tylko dla właściciela widoczne, ale tu pokazujemy info) */}
                    {!song.publiclyVisible && (
                        <div style={{marginTop: '10px'}}>
                            <span style={{border: '1px solid #666', padding: '2px 6px', borderRadius: '4px', fontSize: '0.7rem', color: '#aaa'}}>Prywatny</span>
                        </div>
                    )}
                </div>
            </header>

            {/* ===== KONTROLKI ===== */}
            <section className="song-controls">
                <button className="song-play-button" onClick={handlePlayPause}>
                    <img src={isThisSongPlaying ? pauseIcon : playIcon} alt={isThisSongPlaying ? "Pauza" : "Odtwórz"} />
                </button>

                <button className={`song-control-button ${isFavorite ? 'active' : ''}`} onClick={() => toggleFavorite(song.id)}>
                    <img src={isFavorite ? heartIconOn : heartIconOff} alt="Ulubione" />
                </button>

                <button className={`song-control-button ${isQueuedAnim ? 'active' : ''}`} onClick={handleAddToQueue}>
                    <img src={isQueuedAnim ? queueIconOn : queueIcon} alt="Dodaj do kolejki" />
                </button>

                <div className="song-rating">
                    <button
                        className={`song-rating-button ${currentRating === 'like' ? 'active' : ''}`}
                        onClick={() => rateSong(song.id, 'like')}
                    >
                        <img src={currentRating === 'like' ? likeIconOn : likeIcon} alt="Podoba mi się" />
                    </button>
                    <button
                        className={`song-rating-button ${currentRating === 'dislike' ? 'active' : ''}`}
                        onClick={() => rateSong(song.id, 'dislike')}
                    >
                        <img src={currentRating === 'dislike' ? dislikeIconOn : dislikeIcon} alt="Nie podoba mi się" />
                    </button>
                </div>
            </section>

            {/* ===== SEKCJA KOMENTARZY (Placeholder, bo backend tego jeszcze nie ma) ===== */}
            <section className="comments-section">
                <h2>Komentarze</h2>

                {/* Ponieważ na razie comments to pusta tablica, zawsze wyświetli się empty-message */}
                {song.comments.length === 0 ? (
                    <p className="empty-message">Brak komentarzy. Bądź pierwszy!</p>
                ) : (
                    <>
                        <div className="comment-sort-controls">
                            <button className={commentSort === 'popular' ? 'active' : ''} onClick={() => setCommentSort('popular')}>
                                Najpopularniejsze
                            </button>
                            <button className={commentSort === 'newest' ? 'active' : ''} onClick={() => setCommentSort('newest')}>
                                Najnowsze
                            </button>
                        </div>
                        <ul className="comment-list">
                            {sortedComments.slice(0, visibleComments).map(comment => (
                                <li key={comment.id} className="comment-item">
                                    <img src={comment.avatarUrl || defaultAvatar} alt="avatar" className="comment-avatar" />
                                    <div className="comment-body">
                                        <span className="comment-user">{comment.user}</span>
                                        <p className="comment-text">{comment.text}</p>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </>
                )}
            </section>
        </div>
    );
}

export default SongPage;