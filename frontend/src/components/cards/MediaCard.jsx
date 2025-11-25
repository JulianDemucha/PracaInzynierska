import React from 'react';
import { Link } from 'react-router-dom';
import { usePlayer } from '../../context/PlayerContext'; // 1. Import Contextu
import './MediaCard.css';
import defaultAvatar from '../../assets/images/default-avatar.png';
import playIcon from '../../assets/images/playSong.png';

// 2. Dodajemy prop 'data' (to będzie cały obiekt piosenki/albumu)
function MediaCard({ linkTo, imageUrl, title, subtitle, data }) {

    // 3. Wyciągamy funkcję playSong z contextu
    const { playSong } = usePlayer();

    const handlePlayClick = (e) => {
        e.preventDefault();
        e.stopPropagation(); // Żeby nie wchodziło w link karty po kliknięciu play

        if (data) {
            // 4. Odpalamy piosenkę
            // Dodajemy coverArtUrl, bo MediaCard ma go w propie imageUrl, a Player go potrzebuje
            playSong({ ...data, coverArtUrl: imageUrl });
            console.log(`Odtwarzanie: ${title}`);
        } else {
            console.warn("Brak danych utworu do odtworzenia");
        }
    };

    return (
        <Link to={linkTo} className="media-card-link">
            <div className="media-card">
                <div className="media-card-image-wrapper">
                    <img
                        src={imageUrl || defaultAvatar}
                        alt={title}
                        className="media-card-image"
                    />
                    {/* Przycisk Play wyświetlamy tylko jeśli przekazano dane do odtwarzania */}
                    {data && (
                        <button className="media-play-button" onClick={handlePlayClick}>
                            <img src={playIcon} alt="Odtwórz" />
                        </button>
                    )}
                </div>
                <div className="media-card-info">
                    <h3 className="media-card-title">{title}</h3>
                    <p className="media-card-subtitle">{subtitle}</p>
                </div>
            </div>
        </Link>
    );
}

export default MediaCard;