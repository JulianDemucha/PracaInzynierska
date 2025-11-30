import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './HomePage.css';
import MediaCard from '../components/cards/MediaCard.jsx';
import { getImageUrl } from '../services/imageService.js';
import { getAllSongs } from '../services/songService.js';
import { getAllAlbums } from '../services/albumService.js';

// Lista wszystkich gatunków (statyczna)
const genres = [
    "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
    "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
    "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
];

const ITEMS_IN_ROW = 7;

function HomePage() {
    // Stany rozwijania sekcji
    const [isHitsExpanded, setIsHitsExpanded] = useState(false);
    const [isGenresExpanded, setIsGenresExpanded] = useState(false);
    const [isSongsExpanded, setIsSongsExpanded] = useState(false);
    const [isAlbumsExpanded, setIsAlbumsExpanded] = useState(false);
    const [isPlaylistsExpanded, setIsPlaylistsExpanded] = useState(false);

    // Stany danych (FAKTYCZNE DANE z backendu)
    const [allSongs, setAllSongs] = useState([]);
    const [allAlbums, setAllAlbums] = useState([]);
    const [loading, setLoading] = useState(true);

    // Pobieranie danych z serwisów
    useEffect(() => {
        const fetchHomeData = async () => {
            try {
                setLoading(true);
                // Pobieramy równolegle utwory i albumy
                const [songsData, albumsData] = await Promise.all([
                    getAllSongs(),
                    getAllAlbums()
                ]);

                setAllSongs(songsData);
                setAllAlbums(albumsData);
            } catch (error) {
                console.error("Błąd pobierania danych strony głównej:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchHomeData();
    }, []);

    // Logika wyświetlania:
    // Hity: Na razie bierzemy np. pierwsze 8 piosenek z listy wszystkich (jako symulacja)
    const hitsSongs = allSongs.slice(0, 10);

    const visibleHits = isHitsExpanded ? hitsSongs : hitsSongs.slice(0, ITEMS_IN_ROW);
    const visibleGenres = isGenresExpanded ? genres : genres.slice(0, ITEMS_IN_ROW);
    const visibleSongs = isSongsExpanded ? allSongs : allSongs.slice(0, ITEMS_IN_ROW);
    const visibleAlbums = isAlbumsExpanded ? allAlbums : allAlbums.slice(0, ITEMS_IN_ROW);

    // Playlisty - pusta lista zgodnie z poleceniem "nic nie rób"
    const playlists = [];
    const visiblePlaylists = playlists; // Brak elementów = brak potrzeby slice

    if (loading) {
        return <div className="home-page" style={{display: 'flex', justifyContent: 'center', paddingTop: '50px'}}>Ładowanie...</div>;
    }

    return (
        <div className="home-page">

            {/* 1. SEKCJA: HITY (Symulowane z faktycznych danych) */}
            <section className="home-section">
                <div className="section-header">
                    <h2>🔥 Hity (Najbardziej Polubione)</h2>
                    {hitsSongs.length > ITEMS_IN_ROW && (
                        <span className="see-all" onClick={() => setIsHitsExpanded(!isHitsExpanded)}>
                            {isHitsExpanded ? "ZWIŃ" : "POKAŻ WSZYSTKIE"}
                        </span>
                    )}
                </div>
                <div className="genre-grid">
                    {visibleHits.map((song) => (
                        <MediaCard
                            key={song.id}
                            title={song.title}
                            subtitle={song.artist}
                            // Używamy faktycznego helpera do obrazków
                            imageUrl={getImageUrl(song.coverStorageKeyId)}
                            linkTo={`/song/${song.id}`}
                        />
                    ))}
                </div>
            </section>

            {/* 2. SEKCJA: GATUNKI */}
            <section className="home-section">
                <div className="section-header">
                    <h2>Przeglądaj Gatunki</h2>
                    {genres.length > ITEMS_IN_ROW && (
                        <span className="see-all" onClick={() => setIsGenresExpanded(!isGenresExpanded)}>
                            {isGenresExpanded ? "ZWIŃ" : "POKAŻ WSZYSTKIE"}
                        </span>
                    )}
                </div>
                <div className="genre-grid">
                    {visibleGenres.map((genre) => (
                        <MediaCard
                            key={genre}
                            title={genre}
                            subtitle="Gatunek"
                            imageUrl={`https://placehold.co/400x400/${stringToColor(genre)}/white?text=${genre}`}
                            linkTo={`/genre/${genre.toLowerCase()}`}
                        />
                    ))}
                </div>
            </section>

            {/* 3. SEKCJA: WSZYSTKIE UTWORY */}
            <section className="home-section">
                <div className="section-header">
                    <h2>Wszystkie Utwory</h2>
                    {allSongs.length > ITEMS_IN_ROW && (
                        <span className="see-all" onClick={() => setIsSongsExpanded(!isSongsExpanded)}>
                            {isSongsExpanded ? "ZWIŃ" : "POKAŻ WSZYSTKIE"}
                        </span>
                    )}
                </div>
                <div className="genre-grid">
                    {visibleSongs.length > 0 ? (
                        visibleSongs.map((song) => (
                            <MediaCard
                                key={song.id}
                                title={song.title}
                                subtitle={song.artist}
                                imageUrl={getImageUrl(song.coverStorageKeyId)}
                                linkTo={`/song/${song.id}`}
                            />
                        ))
                    ) : (
                        <p style={{color: '#aaa'}}>Brak utworów w bazie.</p>
                    )}
                </div>
            </section>

            {/* 4. SEKCJA: WSZYSTKIE ALBUMY */}
            <section className="home-section">
                <div className="section-header">
                    <h2>Wszystkie Albumy</h2>
                    {allAlbums.length > ITEMS_IN_ROW && (
                        <span className="see-all" onClick={() => setIsAlbumsExpanded(!isAlbumsExpanded)}>
                            {isAlbumsExpanded ? "ZWIŃ" : "POKAŻ WSZYSTKIE"}
                        </span>
                    )}
                </div>
                <div className="genre-grid">
                    {visibleAlbums.length > 0 ? (
                        visibleAlbums.map((album) => (
                            <MediaCard
                                key={album.id}
                                title={album.title}
                                subtitle={album.artist}
                                imageUrl={getImageUrl(album.coverStorageKeyId)}
                                linkTo={`/album/${album.id}`}
                            />
                        ))
                    ) : (
                        <p style={{color: '#aaa'}}>Brak albumów w bazie.</p>
                    )}
                </div>
            </section>

            {/* 5. SEKCJA: PLAYLISTY (Placeholder) */}
            <section className="home-section">
                <div className="section-header">
                    <h2>Playlisty</h2>
                    {playlists.length > ITEMS_IN_ROW && (
                        <span className="see-all" onClick={() => setIsPlaylistsExpanded(!isPlaylistsExpanded)}>
                            {isPlaylistsExpanded ? "ZWIŃ" : "POKAŻ WSZYSTKIE"}
                        </span>
                    )}
                </div>
                <div className="genre-grid">
                    <div style={{ color: '#aaa', fontStyle: 'italic', gridColumn: '1 / -1' }}>
                        Sekcja playlist w przygotowaniu...
                    </div>
                </div>
            </section>

        </div>
    );
}

// Funkcja pomocnicza do kolorów
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