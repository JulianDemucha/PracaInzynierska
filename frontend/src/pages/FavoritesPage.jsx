import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from "../context/useAuth.js";
import MediaCard from '../components/cards/MediaCard.jsx';
import { getFavouriteSongs } from '../services/songService.js';
import { getImageUrl } from "../services/imageService.js";
import './ProfilePage.css';

const ITEMS_PER_PAGE = 14;

function FavoritesPage() {
    const { currentUser, loading: authLoading } = useAuth();

    const [favoriteSongs, setFavoriteSongs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [totalElements, setTotalElements] = useState(0);

    const fetchFavorites = useCallback(async (pageNumber) => {
        if (!currentUser) return;

        try {
            setLoading(true);
            const data = await getFavouriteSongs(pageNumber, ITEMS_PER_PAGE);

            if (pageNumber === 0) {
                setFavoriteSongs(data.content);
            } else {
                setFavoriteSongs(prev => [...prev, ...data.content]);
            }

            setTotalElements(data.totalElements);
            setHasMore(!data.last);
        } catch (error) {
            console.error("Błąd pobierania ulubionych utworów:", error);
        } finally {
            setLoading(false);
        }
    }, [currentUser]);

    useEffect(() => {
        if (currentUser) {
            fetchFavorites(0);
            setPage(0);
        }
    }, [currentUser, fetchFavorites]);

    const handleLoadMore = () => {
        const nextPage = page + 1;
        setPage(nextPage);
        fetchFavorites(nextPage);
    };

    if (authLoading) {
        return <div className="profile-page"><p>Weryfikacja użytkownika...</p></div>;
    }

    if (!currentUser) {
        return (
            <div className="profile-page">
                <div className="content-section">
                    <h2>Polubione utwory</h2>
                    <p className="empty-tab-message">Zaloguj się, aby zobaczyć swoje ulubione utwory.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="profile-page">
            <header className="profile-header" style={{ minHeight: '150px', padding: '2rem' }}>
                <div className="profile-info">
                    <h1 className="profile-username">Twoje Polubione</h1>
                    <p className="profile-bio-text" style={{ marginTop: '0.5rem' }}>
                        Zbiór wszystkich utworów, które oznaczyłeś serduszkiem.
                    </p>
                </div>
            </header>

            <section className="profile-content">
                <div className="content-section">
                    <h2>
                        Utwory
                        <span className="section-count">({totalElements})</span>
                    </h2>

                    <div className="media-grid">
                        {favoriteSongs.length > 0 ? (
                            favoriteSongs.map(song => (
                                <MediaCard
                                    key={song.id}
                                    linkTo={`/song/${song.id}`}
                                    imageUrl={getImageUrl(song.coverStorageKeyId)}
                                    title={song.title}
                                    subtitle={song.authorUsername || "Nieznany artysta"}
                                    data={song}
                                />
                            ))
                        ) : (
                            !loading && <p className="empty-tab-message">Nie masz jeszcze polubionych utworów.</p>
                        )}
                    </div>

                    {loading && favoriteSongs.length === 0 && (
                        <p style={{ textAlign: 'center', marginTop: '2rem', color: '#b3b3b3' }}>Ładowanie...</p>
                    )}

                    {hasMore && !loading && favoriteSongs.length > 0 && (
                        <div className="expand-controls">
                            <button className="expand-btn" onClick={handleLoadMore}>
                                Pokaż więcej
                            </button>
                        </div>
                    )}

                    {loading && favoriteSongs.length > 0 && (
                        <div className="expand-controls">
                            <span style={{color: '#b3b3b3', fontSize: '0.85rem'}}>Ładowanie więcej...</span>
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
}

export default FavoritesPage;