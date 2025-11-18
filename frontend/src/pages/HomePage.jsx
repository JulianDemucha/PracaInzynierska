import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './HomePage.css';
import MediaCard from '../components/cards/MediaCard.jsx';

const genres = [
    "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
    "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
    "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
];
const ITEMS_IN_ROW = 7;

function HomePage() {
    const [isExpanded, setIsExpanded] = useState(false);

    const visibleGenres = isExpanded ? genres : genres.slice(0, ITEMS_IN_ROW);

    return (
        <div className="home-page">

            <section className="home-section">
                <div className="section-header">
                    <h2>Przeglądaj Gatunki</h2>
                    <span
                        className="see-all"
                        onClick={() => setIsExpanded(!isExpanded)}
                    >
                        {isExpanded ? "ZWIŃ" : "POKAŻ WSZYSTKIE"}
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