import React, { useState, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import './SongPage.css';
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

// --- Przykładowe Dane  ---
const mockSongData = {
    id: 123,
    title: "Kolejny hit (publiczny)",
    artist: { id: 456, name: "Artysta Testowy" },
    coverArtUrl: defaultCover,
    duration: "3:45",
    genres: ["HIP_HOP", "TRAP"],
    comments: [
        { id: 1, user: "FanMuzyki", avatarUrl: "https://placehold.co/40x40/8A2BE2/white?text=F", text: "Niesamowity kawałek!", likes: 15, timestamp: "2025-10-28T10:30:00Z" },
        { id: 2, user: "Krytyk", avatarUrl: "https://placehold.co/40x40/E73C7E/white?text=K", text: "Całkiem niezłe, ale widziałem lepsze.", likes: 2, timestamp: "2025-10-29T11:00:00Z" },
        { id: 3, user: "User3", avatarUrl: null, text: "Test 3", likes: 1, timestamp: "2025-10-20T10:30:00Z" },
        { id: 4, user: "User4", avatarUrl: null, text: "Test 4", likes: 2, timestamp: "2025-10-21T10:30:00Z" },
        { id: 5, user: "User5", avatarUrl: null, text: "Test 5", likes: 3, timestamp: "2025-10-22T10:30:00Z" },
        { id: 6, user: "User6", avatarUrl: null, text: "Test 6", likes: 4, timestamp: "2025-10-23T10:30:00Z" },
        { id: 7, user: "User7", avatarUrl: null, text: "Test 7", likes: 5, timestamp: "2025-10-24T10:30:00Z" },
        { id: 8, user: "User8", avatarUrl: null, text: "Test 8", likes: 6, timestamp: "2025-10-25T10:30:00Z" },
        { id: 9, user: "User9", avatarUrl: null, text: "Test 9", likes: 7, timestamp: "2025-10-26T10:30:00Z" },
        { id: 10, user: "User10", avatarUrl: null, text: "Test 10", likes: 8, timestamp: "2025-10-27T10:30:00Z" },
        { id: 11, user: "User11", avatarUrl: null, text: "Test 11 - Strona 2!", likes: 9, timestamp: "2025-10-28T10:30:00Z" },
    ]
};

function SongPage() {
    const { id } = useParams();

    // Stany dla strony
    const [isPlaying, setIsPlaying] = useState(false);
    const [isFavorite, setIsFavorite] = useState(false); // Serduszko
    const [isQueued, setIsQueued] = useState(false);
    const [rating, setRating] = useState(null); // Kciuki
    const [commentSort, setCommentSort] = useState('popular');
    const [visibleComments, setVisibleComments] = useState(10);
    const [commentLikes, setCommentLikes] = useState({});

    const song = mockSongData;

    // Logika sortowania
    const sortedComments = useMemo(() => {
        const sorted = [...song.comments];
        if (commentSort === 'popular') {
            sorted.sort((a, b) => b.likes - a.likes);
        } else {
            sorted.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
        }
        return sorted;
    }, [song.comments, commentSort]);

    // Logika ocen
    const handleRating = (newRating) => {
        if (rating === newRating) setRating(null);
        else setRating(newRating);
    };

    // Logika polubienia komentarza
    const handleCommentLike = (commentId) => {
        const newLikes = {...commentLikes};
        newLikes[commentId] = !newLikes[commentId];
        setCommentLikes(newLikes);
    };

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
                <button className="song-play-button" onClick={() => setIsPlaying(!isPlaying)}>
                    <img src={isPlaying ? pauseIcon : playIcon} alt={isPlaying ? "Pauza" : "Odtwórz"} />
                </button>
                <button className={`song-control-button ${isFavorite ? 'active' : ''}`} onClick={() => setIsFavorite(!isFavorite)}>
                    <img src={isFavorite ? heartIconOn : heartIconOff} alt="Ulubione" />
                </button>
                <button className={`song-control-button ${isQueued ? 'active' : ''}`} onClick={() => setIsQueued(!isQueued)}>
                    <img src={isQueued ? queueIconOn : queueIcon} alt="Dodaj do kolejki" />
                </button>
                <div className="song-rating">
                    <button
                        className={`song-rating-button ${rating === 'like' ? 'active' : ''}`}
                        onClick={() => handleRating('like')}
                    >
                        <img src={rating === 'like' ? likeIconOn : likeIcon} alt="Podoba mi się" />
                    </button>
                    <button
                        className={`song-rating-button ${rating === 'dislike' ? 'active' : ''}`}
                        onClick={() => handleRating('dislike')}
                    >
                        <img src={rating === 'dislike' ? dislikeIconOn : dislikeIcon} alt="Nie podoba mi się" />
                    </button>
                </div>
            </section>

            {/* ===== 3. SEKCJA KOMENTARZY  ===== */}
            <section className="comments-section">
                <h2>Komentarze</h2>

                {/* Sortowanie  */}
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

                {/* Lista komentarzy */}
                <ul className="comment-list">
                    {song.comments.length === 0 ? (
                        <p className="empty-message">Brak komentarzy. Bądź pierwszy!</p>
                    ) : (
                        sortedComments.slice(0, visibleComments).map(comment => {
                            const isCommentLiked = commentLikes[comment.id] || false;

                            return (
                                <li key={comment.id} className="comment-item">
                                    {/* 1. Awatar */}
                                    <img
                                        src={comment.avatarUrl || defaultAvatar}
                                        alt={`${comment.user} avatar`}
                                        className="comment-avatar"
                                    />
                                    {/* 2. Treść */}
                                    <div className="comment-body">
                                        <span className="comment-user">{comment.user}</span>
                                        <p className="comment-text">{comment.text}</p>
                                    </div>
                                    {/* 3. Akcje */}
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

                {/* Paginacja */}
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