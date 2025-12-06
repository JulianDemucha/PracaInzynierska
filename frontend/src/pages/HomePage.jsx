import React, { useState, useEffect } from 'react';
import MediaCard from '../components/cards/MediaCard.jsx';
import { getImageUrl } from '../services/imageService.js';
import { getAllSongs } from '../services/songService.js';
import { getAllAlbums } from '../services/albumService.js';
import { getAllPlaylists } from '../services/playlistService.js';
import './HomePage.css';

const genres = [
    "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
    "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
    "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
];

const ITEMS_IN_ROW = 7;

const ExpandControls = ({ totalCount, currentLimit, initialLimit, onUpdate }) => {
    if (totalCount <= initialLimit) return null;

    return (
        <div className="expand-controls">
            {currentLimit < totalCount && (
                <>
                    <button
                        className="expand-btn"
                        onClick={() => onUpdate(Math.min(currentLimit + ITEMS_IN_ROW, totalCount))}
                    >
                        Pokaż 7 więcej
                    </button>
                    <button
                        className="expand-btn"
                        onClick={() => onUpdate(totalCount)}
                    >
                        Pokaż wszystkie
                    </button>
                </>
            )}

            {currentLimit > initialLimit && (
                <>
                    <button
                        className="expand-btn"
                        onClick={() => onUpdate(Math.max(currentLimit - ITEMS_IN_ROW, initialLimit))}
                    >
                        Pokaż 7 mniej
                    </button>
                    <button
                        className="expand-btn"
                        onClick={() => onUpdate(initialLimit)}
                    >
                        Zwiń
                    </button>
                </>
            )}
        </div>
    );
};

function HomePage() {
    const [hitsLimit, setHitsLimit] = useState(ITEMS_IN_ROW);
    const [genresLimit, setGenresLimit] = useState(ITEMS_IN_ROW);
    const [songsLimit, setSongsLimit] = useState(ITEMS_IN_ROW);
    const [albumsLimit, setAlbumsLimit] = useState(ITEMS_IN_ROW);
    const [playlistsLimit, setPlaylistsLimit] = useState(ITEMS_IN_ROW);

    const [allSongs, setAllSongs] = useState([]);
    const [allAlbums, setAllAlbums] = useState([]);
    const [allPlaylists, setAllPlaylists] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchHomeData = async () => {
            try {
                setLoading(true);

                const results = await Promise.allSettled([
                    getAllSongs(),
                    getAllAlbums(),
                    getAllPlaylists()
                ]);

                const getData = (result) => result.status === 'fulfilled' ? result.value : [];

                setAllSongs(getData(results[0]));
                setAllAlbums(getData(results[1]));

                const playlistsData = getData(results[2]);
                if (playlistsData && Array.isArray(playlistsData)) {
                    const publicOnly = playlistsData.filter(p => p.publiclyVisible === true);
                    setAllPlaylists(publicOnly);
                } else {
                    setAllPlaylists([]);
                }

            } catch (error) {
                console.error("Błąd pobierania danych strony głównej:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchHomeData();
    }, []);

    const hitsSongs = allSongs.slice(0, 21);

    if (loading) {
        return <div className="home-page loading-container">Ładowanie...</div>;
    }

    return (
        <div className="home-page">

            <section className="home-section">
                <div className="section-header">
                    <h2>
                        🔥 Hity (Najbardziej Polubione)
                        <span className="section-count">({hitsSongs.length})</span>
                    </h2>
                </div>
                <div className="genre-grid">
                    {hitsSongs.slice(0, hitsLimit).map((song) => (
                        <MediaCard
                            key={`hit-${song.id}`}
                            title={song.title}
                            subtitle={song.artist}
                            imageUrl={getImageUrl(song.coverStorageKeyId)}
                            linkTo={`/song/${song.id}`}
                        />
                    ))}
                </div>
                <ExpandControls
                    totalCount={hitsSongs.length}
                    currentLimit={hitsLimit}
                    initialLimit={ITEMS_IN_ROW}
                    onUpdate={setHitsLimit}
                />
            </section>

            <section className="home-section">
                <div className="section-header">
                    <h2>
                        Przeglądaj Gatunki
                        <span className="section-count">({genres.length})</span>
                    </h2>
                </div>
                <div className="genre-grid">
                    {genres.slice(0, genresLimit).map((genre) => (
                        <MediaCard
                            key={genre}
                            title={genre}
                            subtitle="Gatunek"
                            imageUrl={`https://placehold.co/400x400/${stringToColor(genre)}/white?text=${genre}`}
                            linkTo={`/genre/${genre.toLowerCase()}`}
                        />
                    ))}
                </div>
                <ExpandControls
                    totalCount={genres.length}
                    currentLimit={genresLimit}
                    initialLimit={ITEMS_IN_ROW}
                    onUpdate={setGenresLimit}
                />
            </section>

            <section className="home-section">
                <div className="section-header">
                    <h2>
                        Wszystkie Utwory
                        <span className="section-count">({allSongs.length})</span>
                    </h2>
                </div>
                <div className="genre-grid">
                    {allSongs.length > 0 ? (
                        allSongs.slice(0, songsLimit).map((song) => (
                            <MediaCard
                                key={song.id}
                                title={song.title}
                                subtitle={song.artist}
                                imageUrl={getImageUrl(song.coverStorageKeyId)}
                                linkTo={`/song/${song.id}`}
                            />
                        ))
                    ) : (
                        <p className="empty-message">Brak utworów w bazie.</p>
                    )}
                </div>
                <ExpandControls
                    totalCount={allSongs.length}
                    currentLimit={songsLimit}
                    initialLimit={ITEMS_IN_ROW}
                    onUpdate={setSongsLimit}
                />
            </section>

            <section className="home-section">
                <div className="section-header">
                    <h2>
                        Wszystkie Albumy
                        <span className="section-count">({allAlbums.length})</span>
                    </h2>
                </div>
                <div className="genre-grid">
                    {allAlbums.length > 0 ? (
                        allAlbums.slice(0, albumsLimit).map((album) => (
                            <MediaCard
                                key={album.id}
                                title={album.title}
                                subtitle={album.artist}
                                imageUrl={getImageUrl(album.coverStorageKeyId)}
                                linkTo={`/album/${album.id}`}
                            />
                        ))
                    ) : (
                        <p className="empty-message">Brak albumów w bazie.</p>
                    )}
                </div>
                <ExpandControls
                    totalCount={allAlbums.length}
                    currentLimit={albumsLimit}
                    initialLimit={ITEMS_IN_ROW}
                    onUpdate={setAlbumsLimit}
                />
            </section>

            <section className="home-section">
                <div className="section-header">
                    <h2>
                        Playlisty Społeczności
                        <span className="section-count">({allPlaylists.length})</span>
                    </h2>
                </div>
                <div className="genre-grid">
                    {allPlaylists.length > 0 ? (
                        allPlaylists.slice(0, playlistsLimit).map((playlist) => (
                            <MediaCard
                                key={playlist.id}
                                title={playlist.title}
                                subtitle={`${playlist.songsCount || 0} utworów • ${playlist.creatorUsername || 'Nieznany'}`}
                                imageUrl={getImageUrl(playlist.coverStorageKeyId)}
                                linkTo={`/playlist/${playlist.id}`}
                            />
                        ))
                    ) : (
                        <p className="empty-message full-width">Brak publicznych playlist.</p>
                    )}
                </div>
                <ExpandControls
                    totalCount={allPlaylists.length}
                    currentLimit={playlistsLimit}
                    initialLimit={ITEMS_IN_ROW}
                    onUpdate={setPlaylistsLimit}
                />
            </section>

        </div>
    );
}

function stringToColor(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    let color = '';
    for (let i = 0; i < 3; i++) {
        let value = (hash >> (i * 8)) & 0xFF;
        color += ('00' + value.toString(16)).substr(-2);
    }
    return color;
}

export default HomePage;