import React from 'react';
import { Link } from 'react-router-dom';
import { usePlayer } from '../../context/PlayerContext';
import defaultAvatar from '../../assets/images/default-avatar.png';
import playIcon from '../../assets/images/playSong.png';
import './MediaCard.css';

function MediaCard({ linkTo, imageUrl, title, subtitle, data }) {
    const { playSong } = usePlayer();

    const handlePlayClick = (e) => {
        e.preventDefault();
        e.stopPropagation();

        if (data) {
            playSong({ ...data, coverArtUrl: imageUrl });
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