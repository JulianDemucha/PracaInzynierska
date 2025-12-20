import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import MediaCard from '../components/cards/MediaCard.jsx';
import { getSongsByGenre } from '../services/songService.js';
import { getAlbumsByGenre } from '../services/albumService.js';
import { getImageUrl } from "../services/imageService.js";
import { useAuth } from "../context/useAuth.js";
import './GenrePage.css';

function stringToColor(str) {
    if (!str) return '#8A2BE2';
    let hash = 0;
    for (let i = 0; i < str.length; i++) { hash = str.charCodeAt(i) + ((hash << 5) - hash); }
    let color = '#';
    for (let i = 0; i < 3; i++) {
        let value = (hash >> (i * 8)) & 0xFF;
        color += ('00' + value.toString(16)).substr(-2);
    }
    return color;
}

function GenrePage() {
    const { genreName } = useParams();
    const navigate = useNavigate();
    const { currentUser } = useAuth();

    const [activeTab, setActiveTab] = useState('wszystko');

    const [genreSongs, setGenreSongs] = useState([]);
    const [genreAlbums, setGenreAlbums] = useState([]);

    const [songPage, setSongPage] = useState(0);
    const [albumPage, setAlbumPage] = useState(0);

    const [hasMoreSongs, setHasMoreSongs] = useState(true);
    const [hasMoreAlbums, setHasMoreAlbums] = useState(true);

    const [initialLoading, setInitialLoading] = useState(true);
    const [loadingMoreSongs, setLoadingMoreSongs] = useState(false);
    const [loadingMoreAlbums, setLoadingMoreAlbums] = useState(false);

    const [error, setError] = useState(null);

    const PAGE_SIZE = 7;

    const filterVisibleItems = useCallback((items) => {
        if (!Array.isArray(items)) return [];
        return items.filter(item =>
            item.publiclyVisible || (currentUser && currentUser.id === item.authorId)
        );
    }, [currentUser]);

    useEffect(() => {
        const fetchInitialData = async () => {
            setInitialLoading(true);
            setError(null);
            try {
                setSongPage(0);
                setAlbumPage(0);
                setHasMoreSongs(true);
                setHasMoreAlbums(true);

                const [songsResponse, albumsResponse] = await Promise.all([
                    getSongsByGenre(genreName, 0, PAGE_SIZE),
                    getAlbumsByGenre(genreName, 0, PAGE_SIZE)
                ]);

                const rawSongs = songsResponse.content ? songsResponse.content : songsResponse;
                const rawAlbums = albumsResponse.content ? albumsResponse.content : albumsResponse;

                const initialSongs = filterVisibleItems(rawSongs);
                const initialAlbums = filterVisibleItems(rawAlbums);

                setGenreSongs(initialSongs);
                setGenreAlbums(initialAlbums);

                if (rawSongs.length < PAGE_SIZE) setHasMoreSongs(false);
                if (rawAlbums.length < PAGE_SIZE) setHasMoreAlbums(false);

            } catch (err) {
                console.error("Błąd podczas pobierania muzyki:", err);
                setError("Nie udało się pobrać danych.");
            } finally {
                setInitialLoading(false);
            }
        };

        if (genreName) {
            fetchInitialData();
        }
    }, [genreName, currentUser, filterVisibleItems]);
    const loadMoreSongs = async () => {
        if (loadingMoreSongs || !hasMoreSongs) return;
        setLoadingMoreSongs(true);

        try {
            const nextPage = songPage + 1;
            const response = await getSongsByGenre(genreName, nextPage, PAGE_SIZE);
            const rawNewSongs = response.content ? response.content : response;

            if (rawNewSongs.length < PAGE_SIZE) {
                setHasMoreSongs(false);
            }

            const filteredNewSongs = filterVisibleItems(rawNewSongs);
            setGenreSongs(prev => [...prev, ...filteredNewSongs]);
            setSongPage(nextPage);
        } catch (err) {
            console.error("Błąd ładowania utworów:", err);
        } finally {
            setLoadingMoreSongs(false);
        }
    };
    const loadMoreAlbums = async () => {
        if (loadingMoreAlbums || !hasMoreAlbums) return;
        setLoadingMoreAlbums(true);

        try {
            const nextPage = albumPage + 1;
            const response = await getAlbumsByGenre(genreName, nextPage, PAGE_SIZE);
            const rawNewAlbums = response.content ? response.content : response;

            if (rawNewAlbums.length < PAGE_SIZE) {
                setHasMoreAlbums(false);
            }

            const filteredNewAlbums = filterVisibleItems(rawNewAlbums);

            setGenreAlbums(prev => [...prev, ...filteredNewAlbums]);
            setAlbumPage(nextPage);
        } catch (err) {
            console.error("Błąd ładowania albumów:", err);
        } finally {
            setLoadingMoreAlbums(false);
        }
    };

    const formattedGenreTitle = genreName ? genreName.replace('_', ' ').toUpperCase() : '';
    const isEmpty = genreSongs.length === 0 && genreAlbums.length === 0;

    if (initialLoading) {
        return (
            <div className="genre-page loading-state">
                <div className="loading-content">
                    <h2>Ładowanie...</h2>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="genre-page" style={{ padding: '50px', textAlign: 'center', color: 'white' }}>
                <h2>Wystąpił błąd</h2>
                <p>{error}</p>
                <button className="modal-button" onClick={() => window.location.reload()}>
                    Spróbuj ponownie
                </button>
            </div>
        );
    }

    if (!initialLoading && isEmpty) {
        return (
            <div className="genre-page empty-state">
                <div className="empty-modal">
                    <h2>Ups! Pusto tutaj.</h2>
                    <p>Nie znaleźliśmy dostępnej muzyki z gatunku <strong>{formattedGenreTitle}</strong>.</p>
                    <button className="modal-button" onClick={() => navigate('/')}>Wróć na stronę główną</button>
                </div>
            </div>
        );
    }

    return (
        <div className="genre-page">
            <header className="genre-header">
                <div className="genre-banner" style={{ backgroundColor: stringToColor(genreName) }}>
                    <h1>{formattedGenreTitle}</h1>
                </div>
                <div className="genre-info">
                    <h2>Przeglądaj {formattedGenreTitle}</h2>
                    <p>Załadowano: {genreSongs.length + genreAlbums.length} pozycji</p>
                </div>
            </header>

            <nav className="genre-nav">
                <ul className="genre-tabs">
                    <li onClick={() => setActiveTab('wszystko')} className={activeTab === 'wszystko' ? 'active' : ''}>Wszystko</li>
                    <li onClick={() => setActiveTab('utwory')} className={activeTab === 'utwory' ? 'active' : ''}>Utwory ({genreSongs.length})</li>
                    <li onClick={() => setActiveTab('albumy')} className={activeTab === 'albumy' ? 'active' : ''}>Albumy ({genreAlbums.length})</li>
                </ul>
                <div className="genre-nav-border"></div>
            </nav>

            <section className="genre-content">
                {(activeTab === 'wszystko' || activeTab === 'utwory') && genreSongs.length > 0 && (
                    <div className="content-section">
                        <h2>Utwory</h2>
                        <div className="media-grid">
                            {genreSongs.map(item => (
                                <MediaCard
                                    key={item.id}
                                    title={item.title}
                                    subtitle={item.authorUsername || "Nieznany artysta"}
                                    imageUrl={getImageUrl(item.coverStorageKeyId)}
                                    linkTo={`/song/${item.id}`}
                                    data={item}
                                />
                            ))}
                        </div>
                        {hasMoreSongs && (
                            <button
                                className="show-more-button"
                                onClick={loadMoreSongs}
                                disabled={loadingMoreSongs}
                            >
                                {loadingMoreSongs ? 'Ładowanie...' : 'Pokaż więcej utworów'}
                            </button>
                        )}
                    </div>
                )}
                {(activeTab === 'wszystko' || activeTab === 'albumy') && genreAlbums.length > 0 && (
                    <div className="content-section">
                        <h2>Albumy</h2>
                        <div className="media-grid">
                            {genreAlbums.map(item => (
                                <MediaCard
                                    key={item.id}
                                    title={item.title}
                                    subtitle={`Album • ${item.authorName || "Artysta"}`}
                                    imageUrl={getImageUrl(item.coverStorageKeyId)}
                                    linkTo={`/album/${item.id}`}
                                />
                            ))}
                        </div>
                        {hasMoreAlbums && (
                            <button
                                className="show-more-button"
                                onClick={loadMoreAlbums}
                                disabled={loadingMoreAlbums}
                            >
                                {loadingMoreAlbums ? 'Ładowanie...' : 'Pokaż więcej albumów'}
                            </button>
                        )}
                    </div>
                )}
            </section>
        </div>
    );
}

export default GenrePage;