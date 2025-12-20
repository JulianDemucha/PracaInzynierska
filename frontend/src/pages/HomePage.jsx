import React, {useState, useEffect} from 'react';
import MediaCard from '../components/cards/MediaCard.jsx';
import {getImageUrl} from '../services/imageService.js';
import {
    getAllSongs,
    getTrendingSongs,
    getTopLikedSongs,
    getTopViewedSongs,
    getRecommendations
} from '../services/songService.js';
import {getAllAlbums} from '../services/albumService.js';
import {getAllPlaylists} from '../services/playlistService.js';
import {useAuth} from '../context/useAuth.js';
import './HomePage.css';
import getGenres from "../services/genresService.js";

const PAGE_SIZE = 7;

const LIMITS = {
    trending: 21,    // 3 strony
    rec: 56,         // 8 stron
    liked: 56,
    viewed: 56,
    allSongs: Infinity,
    allAlbums: Infinity,
    allPlaylists: Infinity,
    genres: Infinity
};

const ExpandControls = ({canExpand, canCollapse, onShowMore, onCollapse, isLoading}) => {
    if (!canExpand && !canCollapse) return null;

    return (
        <div className="expand-controls">
            {canExpand && (
                <button
                    className="expand-btn"
                    onClick={onShowMore}
                    disabled={isLoading}
                >
                    {isLoading ? 'Ładowanie...' : 'Pokaż więcej'}
                </button>
            )}
            {canCollapse && (
                <button
                    className="expand-btn"
                    onClick={onCollapse}
                    disabled={isLoading}
                >
                    Zwiń
                </button>
            )}
        </div>
    );
};

function HomePage() {
    const {currentUser} = useAuth();
    const [loadingInitial, setLoadingInitial] = useState(true);

    const [sections, setSections] = useState({
        trending: {data: [], page: 0, hasMore: true, loading: false},
        rec: {data: [], page: 0, hasMore: true, loading: false},
        liked: {data: [], page: 0, hasMore: true, loading: false},
        viewed: {data: [], page: 0, hasMore: true, loading: false},
        allSongs: {data: [], page: 0, hasMore: true, loading: false},
        allAlbums: {data: [], page: 0, hasMore: true, loading: false},
        allPlaylists: {data: [], page: 0, hasMore: true, loading: false},
        genres: {data: [], page: 0, hasMore: true, loading: false}
    });

    const updateSection = (key, updates) => {
        setSections(prev => ({
            ...prev,
            [key]: {...prev[key], ...updates}
        }));
    };

    const fetchDataForSection = async (key, page, isInitial = false) => {
        try {
            if (!isInitial) updateSection(key, {loading: true});

            let response;
            switch (key) {
                case 'trending':
                    response = await getTrendingSongs(page, PAGE_SIZE);
                    break;
                case 'liked':
                    response = await getTopLikedSongs(page, PAGE_SIZE);
                    break;
                case 'viewed':
                    response = await getTopViewedSongs(page, PAGE_SIZE);
                    break;
                case 'rec':
                    if (currentUser) response = await getRecommendations(page, PAGE_SIZE);
                    else response = {content: [], last: true};
                    break;
                case 'allSongs':
                    response = await getAllSongs(page, PAGE_SIZE);
                    break;
                case 'allAlbums':
                    response = await getAllAlbums(page, PAGE_SIZE);
                    break;
                case 'allPlaylists':
                    response = await getAllPlaylists(page, PAGE_SIZE);
                    break;
                case 'genres':
                    response = await getGenres();
                    return;
                default:
                    return;
            }

            if (!response) throw new Error("No response");

            const newItems = response.content || [];
            const isLastPage = response.last === true;
            const maxLimit = LIMITS[key];
            const currentCount = isInitial ? 0 : sections[key].data.length;
            const projectedCount = currentCount + newItems.length;
            const hitHardLimit = projectedCount >= maxLimit;
            const filteredItems = key === 'allPlaylists'
                ? newItems.filter(p => p.publiclyVisible)
                : newItems;

            setSections(prev => {
                const existingData = isInitial ? [] : prev[key].data;
                return {
                    ...prev,
                    [key]: {
                        ...prev[key],
                        data: [...existingData, ...filteredItems],
                        page: page,
                        hasMore: !isLastPage && !hitHardLimit,
                        loading: false
                    }
                };
            });
        } catch (error) {
            console.error(`Błąd pobierania dla sekcji ${key}:`, error);
            updateSection(key, {loading: false, hasMore: false});
        }
    };

    useEffect(() => {
        const loadInitialData = async () => {
            setLoadingInitial(true);
            try {
                const genresData = await getGenres();
                updateSection('genres', {
                    data: genresData,
                    hasMore: genresData.length > PAGE_SIZE
                });
            } catch (e) {
                console.error(e);
            }
            const sectionsToLoad = ['trending', 'liked', 'viewed', 'allSongs', 'allAlbums', 'allPlaylists'];
            if (currentUser) sectionsToLoad.push('rec');

            await Promise.allSettled(sectionsToLoad.map(key => fetchDataForSection(key, 0, true)));

            setLoadingInitial(false);
        };

        loadInitialData();
    }, [currentUser]);

    const handleShowMore = (key) => {
        if (key === 'genres') {
            updateSection('genres', {page: sections.genres.page + 1});
            return;
        }
        const nextPage = sections[key].page + 1;
        fetchDataForSection(key, nextPage, false);
    };

    const handleCollapse = (key) => {
        if (key === 'genres') {
            updateSection('genres', {page: 0});
            return;
        }

        setSections(prev => {
            const fullData = prev[key].data;
            return {
                ...prev,
                [key]: {
                    ...prev[key],
                    data: fullData.slice(0, PAGE_SIZE),
                    page: 0,
                    hasMore: true,
                    loading: false
                }
            };
        });
    };

    const renderSection = (title, key, type = 'song') => {
        const sectionState = sections[key];
        if (sectionState.data.length === 0 && key !== 'rec') return null;
        let itemsToDisplay = sectionState.data;
        let canExpand = sectionState.hasMore;

        if (key === 'genres') {
            const limit = (sectionState.page + 1) * PAGE_SIZE;
            itemsToDisplay = sectionState.data.slice(0, limit);
            canExpand = limit < sectionState.data.length;
        } else {
            canExpand = sectionState.hasMore;
        }

        const canCollapse = itemsToDisplay.length > PAGE_SIZE;

        return (
            <section className="home-section">
                <div className="section-header">
                    <h2>{title} <span className="section-count">({sectionState.data.length})</span></h2>
                </div>
                <div className="genre-grid">
                    {itemsToDisplay.map((item, index) => {
                        // Obsługa key dla gatunków (string) vs obiekty
                        const uniqueKey = key === 'genres' ? item : `${type}-${item.id}`;

                        if (key === 'genres') {
                            return (
                                <MediaCard
                                    key={uniqueKey}
                                    title={item}
                                    subtitle="Gatunek"
                                    imageUrl={`https://placehold.co/400x400/${stringToColor(item)}/white?text=${item}`}
                                    linkTo={`/genre/${item.toLowerCase()}`}
                                />
                            );
                        }

                        return (
                            <MediaCard
                                key={uniqueKey}
                                title={item.title || item.name}
                                subtitle={type === 'playlist'
                                    ? `${item.songsCount || 0} utworów • ${item.creatorUsername || 'Nieznany'}`
                                    : item.artist || item.authorUsername || item.creatorUsername}
                                imageUrl={getImageUrl(item.coverStorageKeyId)}
                                linkTo={`/${type}/${item.id}`}
                                data={type === 'song' ? item : null}
                            />
                        );
                    })}
                </div>
                <ExpandControls
                    canExpand={canExpand}
                    canCollapse={canCollapse}
                    isLoading={sectionState.loading}
                    onShowMore={() => handleShowMore(key)}
                    onCollapse={() => handleCollapse(key)}
                />
            </section>
        );
    };

    if (loadingInitial) {
        return <div className="home-page loading-container">Ładowanie...</div>;
    }
    return (
        <div className="home-page">

            {currentUser && sections.rec.data.length > 0 &&
                renderSection("Wybrane dla Ciebie", 'rec', 'song')
            }

            {renderSection("Hity Na Czasie", 'trending', 'song')}

            {renderSection("Najczęściej Odtwarzane", 'viewed', 'song')}

            {renderSection("Ulubione Przez Społeczność", 'liked', 'song')}

            {renderSection("Przeglądaj Gatunki", 'genres', 'genre')}

            {renderSection("Wszystkie Utwory", 'allSongs', 'song')}
            {renderSection("Wszystkie Albumy", 'allAlbums', 'album')}
            {renderSection("Playlisty Społeczności", 'allPlaylists', 'playlist')}

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