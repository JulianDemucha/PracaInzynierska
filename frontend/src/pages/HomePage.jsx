import React, { useState, useEffect } from 'react';
import MediaCard from '../components/cards/MediaCard.jsx';
import { getImageUrl } from '../services/imageService.js';
import {
    getAllSongs,
    getTrendingSongs,
    getTopLikedSongs,
    getTopViewedSongs,
    getRecommendations
} from '../services/songService.js';
import { getAllAlbums } from '../services/albumService.js';
import { getAllPlaylists } from '../services/playlistService.js';
import { useAuth } from '../context/useAuth.js';
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
                        Pokaż więcej
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
                        Pokaż mniej
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
    const { currentUser } = useAuth();

    const [recLimit, setRecLimit] = useState(ITEMS_IN_ROW);
    const [trendingLimit, setTrendingLimit] = useState(ITEMS_IN_ROW);
    const [likedLimit, setLikedLimit] = useState(ITEMS_IN_ROW);
    const [viewedLimit, setViewedLimit] = useState(ITEMS_IN_ROW);

    const [genresLimit, setGenresLimit] = useState(ITEMS_IN_ROW);
    const [songsLimit, setSongsLimit] = useState(ITEMS_IN_ROW);
    const [albumsLimit, setAlbumsLimit] = useState(ITEMS_IN_ROW);
    const [playlistsLimit, setPlaylistsLimit] = useState(ITEMS_IN_ROW);

    const [recommendations, setRecommendations] = useState([]);
    const [trendingSongs, setTrendingSongs] = useState([]);
    const [topLikedSongs, setTopLikedSongs] = useState([]);
    const [topViewedSongs, setTopViewedSongs] = useState([]);

    const [allSongs, setAllSongs] = useState([]);
    const [allAlbums, setAllAlbums] = useState([]);
    const [allPlaylists, setAllPlaylists] = useState([]);

    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchHomeData = async () => {
            try {
                setLoading(true);

                const recPromise = currentUser
                    ? getRecommendations(0)
                    : Promise.resolve({ content: [] });

                const [stdResults, recResult] = await Promise.all([
                    Promise.allSettled([
                        getTrendingSongs(0, 20),
                        getTopLikedSongs(0, 20),
                        getTopViewedSongs(0, 20),
                        getAllSongs(),
                        getAllAlbums(),
                        getAllPlaylists()
                    ]),
                    recPromise.catch(() => ({ content: [] }))
                ]);

                const getData = (result, isPage = false) => {
                    if (result.status === 'fulfilled') {
                        return isPage ? result.value.content : result.value;
                    }
                    return [];
                };

                if (recResult && recResult.content) {
                    setRecommendations(recResult.content);
                }

                setTrendingSongs(getData(stdResults[0], true));
                setTopLikedSongs(getData(stdResults[1], true));
                setTopViewedSongs(getData(stdResults[2], true));

                setAllSongs(getData(stdResults[3]));
                setAllAlbums(getData(stdResults[4]));

                const playlistsData = getData(stdResults[5]);
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
    }, [currentUser]);

    if (loading) {
        return <div className="home-page loading-container">Ładowanie...</div>;
    }

    const renderSection = (title, items, limit, setLimit, type = 'song') => (
        <section className="home-section">
            <div className="section-header">
                <h2>
                    {title}
                    <span className="section-count">({items.length})</span>
                </h2>
            </div>
            <div className="genre-grid">
                {items.length > 0 ? (
                    items.slice(0, limit).map((item) => (
                        <MediaCard
                            key={`${type}-${item.id}`}
                            title={item.title || item.name}
                            subtitle={type === 'playlist'
                                ? `${item.songsCount || 0} utworów • ${item.creatorUsername || 'Nieznany'}`
                                : item.artist || item.authorUsername || item.creatorUsername}
                            imageUrl={getImageUrl(item.coverStorageKeyId)}
                            linkTo={`/${type}/${item.id}`}
                            data={type === 'song' ? item : null}
                        />
                    ))
                ) : (
                    <p className="empty-message">Brak elementów do wyświetlenia.</p>
                )}
            </div>
            <ExpandControls
                totalCount={items.length}
                currentLimit={limit}
                initialLimit={ITEMS_IN_ROW}
                onUpdate={setLimit}
            />
        </section>
    );

    return (
        <div className="home-page">

            {currentUser && recommendations.length > 0 &&
                renderSection("Wybrane dla Ciebie", recommendations, recLimit, setRecLimit, 'song')
            }

            {renderSection("Hity Na Czasie", trendingSongs, trendingLimit, setTrendingLimit, 'song')}

            {renderSection("Najczęściej Odtwarzane", topViewedSongs, viewedLimit, setViewedLimit, 'song')}

            {renderSection("Ulubione Przez Społeczność", topLikedSongs, likedLimit, setLikedLimit, 'song')}

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

            {renderSection("Wszystkie Utwory", allSongs, songsLimit, setSongsLimit, 'song')}

            {renderSection("Wszystkie Albumy", allAlbums, albumsLimit, setAlbumsLimit, 'album')}

            {renderSection("Playlisty Społeczności", allPlaylists, playlistsLimit, setPlaylistsLimit, 'playlist')}

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