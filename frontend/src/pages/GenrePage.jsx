import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './GenrePage.css';
import MediaCard from '../components/cards/MediaCard.jsx';

// Limit elementów wyświetlanych w stanie zwiniętym
const MAX_ITEMS_PER_SECTION = 7;

// --- BAZA DANYCH  ---
const mockAllMedia = [
    { id: "p1", type: "playlist", title: "Hity Pop 2024", subtitle: "Najlepsze", genre: ["pop", "dance"], img: "https://placehold.co/300x300/FF007F/white?text=PopHits" },
    { id: "a1", type: "album", title: "Rock Legends", subtitle: "The Classics", genre: ["rock", "classic_rock", "metal"], img: "https://placehold.co/300x300/000000/white?text=Rock" },
    { id: "s1", type: "song", title: "Jazz Vibes", subtitle: "Smooth Jazz", genre: ["jazz", "instrumental"], img: "https://placehold.co/300x300/8A2BE2/white?text=Jazz" },
    { id: "p2", type: "playlist", title: "Hip-Hop Radar", subtitle: "Premiery", genre: ["hip_hop", "trap", "rnb"], img: "https://placehold.co/300x300/FFA500/white?text=HipHop" },
    { id: "a2", type: "album", title: "Pop Album", subtitle: "Artist X", genre: ["pop"], img: "https://placehold.co/300x300/1DB954/white?text=PopAlb" },
    { id: "a2", type: "album", title: "Pop Album", subtitle: "Artist X", genre: ["pop"], img: "https://placehold.co/300x300/1DB954/white?text=PopAlb" },
    { id: "a2", type: "album", title: "Pop Album", subtitle: "Artist X", genre: ["pop"], img: "https://placehold.co/300x300/1DB954/white?text=PopAlb" },
    { id: "a2", type: "album", title: "Pop Album", subtitle: "Artist X", genre: ["pop"], img: "https://placehold.co/300x300/1DB954/white?text=PopAlb" },
    { id: "a2", type: "album", title: "Pop Album", subtitle: "Artist X", genre: ["pop"], img: "https://placehold.co/300x300/1DB954/white?text=PopAlb" },
    { id: "a2", type: "album", title: "Pop Album", subtitle: "Artist X", genre: ["pop"], img: "https://placehold.co/300x300/1DB954/white?text=PopAlb" },
    { id: "a2", type: "album", title: "Pop Album", subtitle: "Artist X", genre: ["pop"], img: "https://placehold.co/300x300/1DB954/white?text=PopAlb" },
    { id: "a2", type: "album", title: "Pop Album", subtitle: "Artist X", genre: ["pop"], img: "https://placehold.co/300x300/1DB954/white?text=PopAlb" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s2", type: "song", title: "Electronic Chill", subtitle: "Relax", genre: ["electronic", "pop"], img: "https://placehold.co/300x300/00FFFF/white?text=Electro" },
    { id: "s3", type: "song", title: "Rock Anthem", subtitle: "Band Y", genre: ["rock"], img: "https://placehold.co/300x300/6A0DAD/white?text=RockSong" },
];

function GenrePage() {
    const { genreName } = useParams();
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('wszystko');

    // --- NOWE STANY DO KONTROLI ZWIJANIA/ROZWIJANIA ---
    const [showAllSongs, setShowAllSongs] = useState(false);
    const [showAllAlbums, setShowAllAlbums] = useState(false);
    const [showAllPlaylists, setShowAllPlaylists] = useState(false);

    const genreContent = mockAllMedia.filter(item => {
        const itemGenres = Array.isArray(item.genre) ? item.genre : [item.genre];
        return itemGenres.some(g => g.toLowerCase() === genreName.toLowerCase());
    });

    const genreSongs = genreContent.filter(item => item.type === 'song');
    const genreAlbums = genreContent.filter(item => item.type === 'album');
    const genrePlaylists = genreContent.filter(item => item.type === 'playlist');

    const formattedGenreTitle = genreName.replace('_', ' ').toUpperCase();

    // --- MODAL ---
    if (genreContent.length === 0) {
        return (
            <div className="genre-page empty-state">
                <div className="empty-modal">
                    <div className="modal-icon">⚠️</div>
                    <h2>Ups! Pusto tutaj.</h2>
                    <p>Nie znaleźliśmy muzyki z gatunku <strong>{formattedGenreTitle}</strong>.</p>
                    <button className="modal-button" onClick={() => navigate('/')}>Wróć na stronę główną</button>
                </div>
            </div>
        );
    }

    return (
        <div className="genre-page">
            {/* Nagłówek */}
            <header className="genre-header">
                <div className="genre-banner" style={{backgroundColor: stringToColor(genreName)}}>
                    <h1>{formattedGenreTitle}</h1>
                </div>
                <div className="genre-info">
                    <h2>Przeglądaj {formattedGenreTitle}</h2>
                    <p>Znaleziono: {genreContent.length} pozycji</p>
                </div>
            </header>

            {/* NAWIGACJA */}
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
                {(activeTab === 'wszystko' || activeTab === 'utwory') && genreSongs.length > 0 && (
                    <div className="content-section">
                        <h2>Utwory</h2>
                        <div className="media-grid">
                            {(showAllSongs ? genreSongs : genreSongs.slice(0, MAX_ITEMS_PER_SECTION)).map(item => (
                                <MediaCard
                                    key={item.id}
                                    title={item.title}
                                    subtitle={item.subtitle}
                                    imageUrl={item.img}
                                    linkTo={`/song/${item.id}`}
                                />
                            ))}
                        </div>
                        {/* Przycisk "Pokaż więcej" */}
                        {genreSongs.length > MAX_ITEMS_PER_SECTION && (
                            <button
                                className="show-more-button"
                                onClick={() => setShowAllSongs(!showAllSongs)}
                            >
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
                                    subtitle={item.subtitle}
                                    imageUrl={item.img}
                                    linkTo={`/album/${item.id}`}
                                />
                            ))}
                        </div>
                        {genreAlbums.length > MAX_ITEMS_PER_SECTION && (
                            <button
                                className="show-more-button"
                                onClick={() => setShowAllAlbums(!showAllAlbums)}
                            >
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
                                    subtitle={item.subtitle}
                                    imageUrl={item.img}
                                    linkTo={`/playlist/${item.id}`}
                                />
                            ))}
                        </div>
                        {genrePlaylists.length > MAX_ITEMS_PER_SECTION && (
                            <button
                                className="show-more-button"
                                onClick={() => setShowAllPlaylists(!showAllPlaylists)}
                            >
                                {showAllPlaylists ? 'Zwiń' : 'Pokaż więcej'}
                            </button>
                        )}
                    </div>
                )}
            </section>
        </div>
    );
}

// Funkcja pomocnicza do generowania koloru
function stringToColor(str) {
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