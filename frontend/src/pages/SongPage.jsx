import React, { useState, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import './SongPage.css';
import { usePlayer } from '../context/PlayerContext'; // 1. Import Contextu

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

const mockSongDatabase = {
    "123": {
        id: "123",
        title: "Kolejny hit (publiczny)",
        artist: { id: "456", name: "Artysta Testowy" },
        coverArtUrl: "https://placehold.co/300x300/53346D/white?text=Hit",
        duration: "3:45",
        genres: ["HIP_HOP", "TRAP"],
        comments: [
            { id: 1, user: "FanMuzyki", avatarUrl: "https://placehold.co/40x40/8A2BE2/white?text=F", text: "Niesamowity kawałek!", likes: 15, timestamp: "2025-10-28T10:30:00Z" },
            { id: 2, user: "Krytyk", avatarUrl: "https://placehold.co/40x40/E73C7E/white?text=K", text: "Całkiem niezłe.", likes: 2, timestamp: "2025-10-29T11:00:00Z" },
            { id: 3, user: "User11", avatarUrl: null, text: "Test 11 - Strona 2!", likes: 9, timestamp: "2025-10-28T10:30:00Z" },
        ]
    },
    "456": { // Inna piosenka
        id: "456",
        title: "Inny Utwór",
        artist: { id: "789", name: "Inny Artysta" },
        coverArtUrl: "https://placehold.co/300x300/1DB954/white?text=Inny",
        duration: "2:15",
        genres: ["POP"],
        comments: [] // Brak komentarzy
    }
};

function SongPage() {
    const { id } = useParams();

    // 2. POBIERAMY DANE Z GLOBALNEGO ODTWARZACZA
    const {
        currentSong,
        isPlaying,
        playSong,
        pause,
        addToQueue,
        favorites, toggleFavorite, // Serduszka
        ratings, rateSong          // Łapki
    } = usePlayer();

    // Lokalne stany (tylko dla UI/Animacji i Komentarzy)
    const [isQueuedAnim, setIsQueuedAnim] = useState(false); // Zmiana nazwy by nie mylić z logiką
    const [commentSort, setCommentSort] = useState('popular');
    const [visibleComments, setVisibleComments] = useState(10);
    const [commentLikes, setCommentLikes] = useState({});

    const song = mockSongDatabase[id];

    const sortedComments = useMemo(() => {
        if (!song) return [];
        const sorted = [...song.comments];
        if (commentSort === 'popular') {
            sorted.sort((a, b) => b.likes - a.likes);
        } else {
            sorted.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        }
        return sorted;
    }, [song, commentSort]);


    // --- LOGIKA PODPIĘTA POD CONTEXT ---

    // 1. Sprawdzamy czy ta piosenka gra w globalnym playerze
    const isThisSongActive = currentSong?.id === song?.id;
    const isThisSongPlaying = isThisSongActive && isPlaying;

    const handlePlayPause = () => {
        if (isThisSongPlaying) {
            pause();
        } else {
            playSong(song);
        }
    };

    // 2. Sprawdzamy status ulubionych i ocen z Contextu
    const isFavorite = !!favorites[song?.id];
    const currentRating = ratings[song?.id]; // 'like', 'dislike' lub undefined

    // 3. Logika dodawania do kolejki (z zachowaniem animacji)
    const handleAddToQueue = () => {
        if (isQueuedAnim) return;

        addToQueue(song); // Dodaje do globalnej kolejki
        console.log("Dodano do kolejki:", song.title);

        // Animacja ikonki
        setIsQueuedAnim(true);
        setTimeout(() => {
            setIsQueuedAnim(false);
        }, 1500);
    };

    // Logika polubienia komentarza (zostaje lokalna)
    const handleCommentLike = (commentId) => {
        const newLikes = {...commentLikes};
        newLikes[commentId] = !newLikes[commentId];
        setCommentLikes(newLikes);
    };

    // --- Zabezpieczenie, jeśli ID jest złe ---
    if (!song) {
        return (
            <div className="song-page">
                <h2>404 - Nie znaleziono piosenki</h2>
                <p>Piosenka o ID: {id} nie istnieje.</p>
            </div>
        );
    }

    return (
        <div className="song-page">
            {/* ===== 1. NAGŁÓWEK ===== */}
            <header className="song-header">
                <img src={song.coverArtUrl || defaultCover} alt={song.title} className="song-cover-art" />
                <div className="song-details">
                    <span className="song-type">UTWÓR</span>
                    <h1>{song.title}</h1>
                    <div className="song-meta">
                        <Link to={`/artist/${song.artist.id}`} className="song-artist">{song.artist.name}</Link>
                        <span>•</span>
                        <span className="song-duration">{song.duration}</span>
                    </div>
                    <div className="genre-tags">
                        {song.genres.map(genre => (
                            <span key={genre} className="genre-pill">{genre}</span>
                        ))}
                    </div>
                </div>
            </header>

            {/* ===== 2. KONTROLKI ===== */}
            <section className="song-controls">
                <button className="song-play-button" onClick={handlePlayPause}>
                    <img src={isThisSongPlaying ? pauseIcon : playIcon} alt={isThisSongPlaying ? "Pauza" : "Odtwórz"} />
                </button>

                {/* Serduszko korzysta teraz z globalnego toggleFavorite */}
                <button className={`song-control-button ${isFavorite ? 'active' : ''}`} onClick={() => toggleFavorite(song.id)}>
                    <img src={isFavorite ? heartIconOn : heartIconOff} alt="Ulubione" />
                </button>

                {/* Kolejka z animacją */}
                <button className={`song-control-button ${isQueuedAnim ? 'active' : ''}`} onClick={handleAddToQueue}>
                    <img src={isQueuedAnim ? queueIconOn : queueIcon} alt="Dodaj do kolejki" />
                </button>

                {/* Łapki korzystają z globalnego rateSong */}
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

            {/* ===== 3. SEKCJA KOMENTARZY (Bez zmian logicznych) ===== */}
            <section className="comments-section">
                <h2>Komentarze</h2>

                <div className="comment-sort-controls">
                    <button
                        className={commentSort === 'popular' ? 'active' : ''}
                        onClick={() => setCommentSort('popular')}
                    >
                        Najpopularniejsze
                    </button>
                    <button
                        className={commentSort === 'newest' ? 'active' : ''}
                        onClick={() => setCommentSort('newest')}
                    >
                        Najnowsze
                    </button>
                </div>

                <ul className="comment-list">
                    {song.comments.length === 0 ? (
                        <p className="empty-message">Brak komentarzy. Bądź pierwszy!</p>
                    ) : (
                        sortedComments.slice(0, visibleComments).map(comment => {
                            const isCommentLiked = commentLikes[comment.id] || false;

                            return (
                                <li key={comment.id} className="comment-item">
                                    <img
                                        src={comment.avatarUrl || defaultAvatar}
                                        alt={`${comment.user} avatar`}
                                        className="comment-avatar"
                                    />
                                    <div className="comment-body">
                                        <span className="comment-user">{comment.user}</span>
                                        <p className="comment-text">{comment.text}</p>
                                    </div>
                                    <div className="comment-actions">
                                        <button
                                            className={`comment-like-button ${isCommentLiked ? 'active' : ''}`}
                                            onClick={() => handleCommentLike(comment.id)}
                                        >
                                            <img src={isCommentLiked ? likeIconOn : likeIcon} alt="Polub" />
                                        </button>
                                        <span className="comment-likes-count">{comment.likes}</span>
                                    </div>
                                </li>
                            );
                        })
                    )}
                </ul>

                <div className="comment-pagination">
                    {song.comments.length > visibleComments && (
                        <button className="pagination-button" onClick={() => setVisibleComments(prev => prev + 10)}>
                            Pokaż więcej
                        </button>
                    )}
                    {visibleComments > 10 && (
                        <button className="pagination-button" onClick={() => setVisibleComments(10)}>
                            Pokaż mniej
                        </button>
                    )}
                </div>
            </section>
        </div>
    );
}

export default SongPage;