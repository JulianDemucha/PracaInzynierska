import React from 'react';
import { Link } from 'react-router-dom';
import './MediaCard.css';
import defaultAvatar from '../../assets/images/default-avatar.png';
import playIcon from '../../assets/images/playSong.png';

function MediaCard({ linkTo, imageUrl, title, subtitle }) {
    const handlePlayClick = (e) => {
        e.preventDefault();
        e.stopPropagation();

        // TODO połączyć z PlayerContext, aby odtworzyć utwór w PlayerBarze. Na razie logujemy do konsoli.
        console.log(`Odtwarzanie: ${title}`);
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
                    <button className="media-play-button" onClick={handlePlayClick}>
                        <img src={playIcon} alt="Odtwórz" />
                    </button>
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