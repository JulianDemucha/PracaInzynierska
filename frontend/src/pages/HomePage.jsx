import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './HomePage.css';
import MediaCard from '../components/cards/MediaCard.jsx';

// Lista wszystkich gatunków (bez zmian)
const genres = [
    "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
    "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
    "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
];

// --- MOCK DANYCH: HITY (Rozszerzone, by testować limit 7) ---
const mockHitsSongs = [
    { id: "s1_h", type: "song", title: "Global Hit #1", subtitle: "Top Chart 2025", img: "https://placehold.co/400x400/800080/white?text=HIT+1" },
    { id: "s2_h", type: "song", title: "Dance Floor Anthem", subtitle: "Top Chart 2024", img: "https://placehold.co/400x400/32CD32/white?text=HIT+2" },
    { id: "s3_h", type: "song", title: "Indie Summer Vibe", subtitle: "Trendsetter", img: "https://placehold.co/400x400/FFD700/black?text=HIT+3" },
    { id: "s4_h", type: "song", title: "Chillout Masterpiece", subtitle: "Relaxation", img: "https://placehold.co/400x400/4682B4/white?text=HIT+4" },
    { id: "s5_h", type: "song", title: "Pop Chart Dominator", subtitle: "Global Top", img: "https://placehold.co/400x400/DC143C/white?text=HIT+5" },
    { id: "s6_h", type: "song", title: "Viral Sensation", subtitle: "TikTok Hit", img: "https://placehold.co/400x400/F08080/white?text=HIT+6" },
    { id: "s7_h", type: "song", title: "Rock Power Ballad", subtitle: "Epic", img: "https://placehold.co/400x400/A0522D/white?text=HIT+7" },
    { id: "s8_h", type: "song", title: "The Eighth Wonder", subtitle: "New Entry", img: "https://placehold.co/400x400/87CEEB/black?text=HIT+8" },
    { id: "s9_h", type: "song", title: "Ninth Heaven", subtitle: "Chill", img: "https://placehold.co/400x400/DDA0DD/white?text=HIT+9" },
];
// ----------------------------------------------------------------------

const ITEMS_IN_ROW = 7;

function HomePage() {
    // Stan dla gatunków
    const [isGenresExpanded, setIsGenresExpanded] = useState(false);
    // Stan dla hitów (NOWY)
    const [isHitsExpanded, setIsHitsExpanded] = useState(false);

    // Logika dla gatunków (bez zmian)
    const visibleGenres = isGenresExpanded ? genres : genres.slice(0, ITEMS_IN_ROW);

    // Logika dla hitów (NOWA)
    const visibleHits = isHitsExpanded ? mockHitsSongs : mockHitsSongs.slice(0, ITEMS_IN_ROW);

    return (
        <div className="home-page">

            {/* --- SEKCJA: HITY (Najbardziej Polubione Utwory) --- */}
            <section className="home-section">
                <div className="section-header">
                    <h2>🔥 Hity (Najbardziej Polubione)</h2>
                    {mockHitsSongs.length > ITEMS_IN_ROW && (
                        <span
                            className="see-all"
                            onClick={() => setIsHitsExpanded(!isHitsExpanded)}
                        >
                            {isHitsExpanded ? "ZWIŃ" : "POKAŻ WSZYSTKIE"}
                        </span>
                    )}
                </div>

                <div className="genre-grid">
                    {visibleHits.map((song) => (
                        <MediaCard
                            key={song.id}
                            title={song.title}
                            subtitle={song.subtitle}
                            imageUrl={song.img}
                            linkTo={`/song/${song.id}`}
                        />
                    ))}
                </div>
            </section>

            {/* --- SEKCJA PRZEGLĄDAJ GATUNKI --- */}
            <section className="home-section">
                <div className="section-header">
                    <h2>Przeglądaj Gatunki</h2>
                    <span
                        className="see-all"
                        onClick={() => setIsGenresExpanded(!isGenresExpanded)}
                    >
                        {isGenresExpanded ? "ZWIŃ" : "POKAŻ WSZYSTKIE"}
                    </span>
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
        </div>
    );
}

// Funkcja pomocnicza do kolorów (bez zmian)
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