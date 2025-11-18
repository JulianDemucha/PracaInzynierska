import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './GenrePage.css';
import MediaCard from '../components/cards/MediaCard.jsx';

// --- BAZA DANYCH (Teraz obsługuje tablice gatunków) ---
const mockAllMedia = [
    {
        id: "p1",
        type: "playlist",
        title: "Hity Pop 2024",
        subtitle: "Najlepsze",
        genre: ["pop", "dance"], // Tablica gatunków
        img: "https://placehold.co/300x300/FF007F/white?text=PopHits"
    },
    {
        id: "a1",
        type: "album",
        title: "Rock Legends",
        subtitle: "The Classics",
        genre: ["rock", "classic_rock", "metal"], // Aż 3 gatunki
        img: "https://placehold.co/300x300/000000/white?text=Rock"
    },
    {
        id: "s1",
        type: "song",
        title: "Jazz Vibes",
        subtitle: "Smooth Jazz",
        genre: ["jazz", "instrumental"],
        img: "https://placehold.co/300x300/8A2BE2/white?text=Jazz"
    },
    {
        id: "p2",
        type: "playlist",
        title: "Hip-Hop Radar",
        subtitle: "Premiery",
        genre: ["hip_hop", "trap", "rnb"], // Wyświetli się dla każdego z tych 3
        img: "https://placehold.co/300x300/FFA500/white?text=HipHop"
    },
    {
        id: "a2",
        type: "album",
        title: "Pop Album",
        subtitle: "Artist X",
        genre: ["pop"], // Pojedynczy też działa jako tablica
        img: "https://placehold.co/300x300/1DB954/white?text=PopAlb"
    },
    {
        id: "s2",
        type: "song",
        title: "Electronic Chill",
        subtitle: "Relax",
        genre: ["electronic", "pop"],
        img: "https://placehold.co/300x300/00FFFF/white?text=Electro"
    },
];

function GenrePage() {
    const { genreName } = useParams();
    const navigate = useNavigate();

    // --- LOGIKA FILTROWANIA (WIELOKROTNE GATUNKI) ---
    const filteredMedia = mockAllMedia.filter(item => {
        // 1. Pobieramy gatunki elementu (zabezpieczenie: jeśli to string, zamień na tablicę)
        const itemGenres = Array.isArray(item.genre) ? item.genre : [item.genre];

        // 2. Sprawdzamy, czy CZYKOLWIEK z tablicy itemGenres pasuje do szukanego genreName
        // Używamy .some() - zwraca true, jeśli chociaż jeden element spełnia warunek
        return itemGenres.some(g => g.toLowerCase() === genreName.toLowerCase());
    });

    // Formatowanie nazwy gatunku (np. hip_hop -> HIP HOP)
    const formattedGenreTitle = genreName.replace('_', ' ').toUpperCase();

    // --- MODAL BRAKU WYNIKÓW ---
    if (filteredMedia.length === 0) {
        return (
            <div className="genre-page empty-state">
                <div className="empty-modal">
                    <div className="modal-icon">⚠️</div>
                    <h2>Ups! Pusto tutaj.</h2>
                    <p>Nie znaleźliśmy jeszcze żadnej muzyki z gatunku <strong>{formattedGenreTitle}</strong>.</p>
                    <p>Spróbuj poszukać czegoś innego.</p>
                    <button className="modal-button" onClick={() => navigate('/')}>
                        Wróć na stronę główną
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="genre-page">
            {/* Nagłówek Gatunku */}
            <header className="genre-header">
                <div className="genre-banner" style={{backgroundColor: stringToColor(genreName)}}>
                    <h1>{formattedGenreTitle}</h1>
                </div>
                <div className="genre-info">
                    <h2>Najlepsze z kategorii {formattedGenreTitle}</h2>
                    <p>Znaleziono {filteredMedia.length} pozycji</p>
                </div>
            </header>

            {/* Siatka wyników */}
            <section className="genre-content">
                <div className="media-grid">
                    {filteredMedia.map(item => (
                        <MediaCard
                            key={item.id}
                            title={item.title}
                            subtitle={item.subtitle}
                            imageUrl={item.img}
                            linkTo={`/${item.type}/${item.id}`}
                        />
                    ))}
                </div>
            </section>
        </div>
    );
}

// Funkcja pomocnicza do generowania koloru
function stringToColor(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    let color = '#';
    for (let i = 0; i < 3; i++) {
        let value = (hash >> (i * 8)) & 0xFF;
        color += ('00' + value.toString(16)).substr(-2);
    }
    return color;
}

export default GenrePage;