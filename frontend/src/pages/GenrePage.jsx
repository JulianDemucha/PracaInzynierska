import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './GenrePage.css';
import MediaCard from '../components/cards/MediaCard.jsx';
import { getSongsByGenre } from '../services/songService.js';
import { getAlbumsByGenre } from '../services/albumService.js';
import { getImageUrl } from "../services/imageService.js";
import defaultAvatar from '../assets/images/default-avatar.png';
import { useAuth } from "../context/useAuth.js"; // 1. Importujemy kontekst autoryzacji

const MAX_ITEMS_PER_SECTION = 7;

function GenrePage() {
    const { genreName } = useParams();
    const navigate = useNavigate();
    const { currentUser } = useAuth(); // 2. Pobieramy aktualnego użytkownika
    const [activeTab, setActiveTab] = useState('wszystko');

    // --- STANY DANYCH ---
    const [genreSongs, setGenreSongs] = useState([]);
    const [genreAlbums, setGenreAlbums] = useState([]);
    const [genrePlaylists, setGenrePlaylists] = useState([]);

    // --- STANY UI ---
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showAllSongs, setShowAllSongs] = useState(false);
    const [showAllAlbums, setShowAllAlbums] = useState(false);
    const [showAllPlaylists, setShowAllPlaylists] = useState(false);

    // --- POBIERANIE DANYCH Z API ---
    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(null);
            try {
                const [songsData, albumsData] = await Promise.all([
                    getSongsByGenre(genreName),
                    getAlbumsByGenre(genreName)
                ]);


                const filteredSongs = songsData.filter(song =>
                    song.publiclyVisible || (currentUser && currentUser.id === song.authorId)
                );

                const filteredAlbums = albumsData.filter(album =>
                    album.publiclyVisible || (currentUser && currentUser.id === album.authorId)
                );

                setGenreSongs(filteredSongs);
                setGenreAlbums(filteredAlbums);

            } catch (err) {
                console.error("Błąd podczas pobierania muzyki:", err);
                setError("Nie udało się pobrać danych.");
            } finally {
                setLoading(false);
            }
        };

        if (genreName) {
            fetchData();
        }
    }, [genreName, currentUser]);

    const formattedGenreTitle = genreName.replace('_', ' ').toUpperCase();
    const isEmpty = genreSongs.length === 0 && genreAlbums.length === 0 && genrePlaylists.length === 0;

    if (loading) {
        return (
            <div className="genre-page loading-state">
                <div style={{ padding: '50px', textAlign: 'center', color: 'white' }}>
                    <h2>Ładowanie...</h2>
                </div>
            </div>
        );
    }

    if (!loading && isEmpty) {
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
                <div className="genre-banner" style={{backgroundColor: stringToColor(genreName)}}>
                    <h1>{formattedGenreTitle}</h1>
                </div>
                <div className="genre-info">
                    <h2>Przeglądaj {formattedGenreTitle}</h2>
                    <p>Znaleziono: {genreSongs.length + genreAlbums.length + genrePlaylists.length} pozycji</p>
                </div>
            </header>

            <nav className="genre-nav">
                <ul className="genre-tabs">
                    <li onClick={() => setActiveTab('wszystko')} className={activeTab === 'wszystko' ? 'active' : ''}>Wszystko</li>
                    <li onClick={() => setActiveTab('utwory')} className={activeTab === 'utwory' ? 'active' : ''}>Utwory ({genreSongs.length})</li>
                    <li onClick={() => setActiveTab('albumy')} className={activeTab === 'albumy' ? 'active' : ''}>Albumy ({genreAlbums.length})</li>
                    <li onClick={() => setActiveTab('playlisty')} className={activeTab === 'playlisty' ? 'active' : ''}>Playlisty ({genrePlaylists.length})</li>
                </ul>
                <div className="genre-nav-border"></div>
            </nav>

            <section className="genre-content">
                {/* --- SEKCJA UTWORÓW --- */}
                {(activeTab === 'wszystko' || activeTab === 'utwory') && genreSongs.length > 0 && (
                    <div className="content-section">
                        <h2>Utwory</h2>
                        <div className="media-grid">
                            {(showAllSongs ? genreSongs : genreSongs.slice(0, MAX_ITEMS_PER_SECTION)).map(item => (
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
                        {genreSongs.length > MAX_ITEMS_PER_SECTION && (
                            <button className="show-more-button" onClick={() => setShowAllSongs(!showAllSongs)}>
                                {showAllSongs ? 'Zwiń' : 'Pokaż więcej'}
                            </button>
                        )}
                    </div>
                )}

                {/* --- SEKCJA ALBUMÓW --- */}
                {(activeTab === 'wszystko' || activeTab === 'albumy') && genreAlbums.length > 0 && (
                    <div className="content-section">
                        <h2>Albumy</h2>
                        <div className="media-grid">
                            {(showAllAlbums ? genreAlbums : genreAlbums.slice(0, MAX_ITEMS_PER_SECTION)).map(item => (
                                <MediaCard
                                    key={item.id}
                                    title={item.title}
                                    subtitle={`Album • ${item.authorName || "Artysta"}`}
                                    imageUrl={getImageUrl(item.coverStorageKeyId)}
                                    linkTo={`/album/${item.id}`}
                                />
                            ))}
                        </div>
                        {genreAlbums.length > MAX_ITEMS_PER_SECTION && (
                            <button className="show-more-button" onClick={() => setShowAllAlbums(!showAllAlbums)}>
                                {showAllAlbums ? 'Zwiń' : 'Pokaż więcej'}
                            </button>
                        )}
                    </div>
                )}

                {/* --- SEKCJA PLAYLIST --- */}
                {(activeTab === 'wszystko' || activeTab === 'playlisty') && genrePlaylists.length > 0 && (
                    <div className="content-section">
                        <h2>Playlisty</h2>
                        <div className="media-grid">
                            {(showAllPlaylists ? genrePlaylists : genrePlaylists.slice(0, MAX_ITEMS_PER_SECTION)).map(item => (
                                <MediaCard
                                    key={item.id}
                                    title={item.title}
                                    subtitle={item.description || "Playlista"}
                                    imageUrl={defaultAvatar}
                                    linkTo={`/playlist/${item.id}`}
                                />
                            ))}
                        </div>
                    </div>
                )}
            </section>
        </div>
    );
}

function stringToColor(str) {
    if(!str) return '#8A2BE2';
    let hash = 0;
    for (let i = 0; i < str.length; i++) { hash = str.charCodeAt(i) + ((hash << 5) - hash); }
    let color = '#';
    for (let i = 0; i < 3; i++) {
        let value = (hash >> (i * 8)) & 0xFF;
        color += ('00' + value.toString(16)).substr(-2);
    }
    return color;
}

export default GenrePage;