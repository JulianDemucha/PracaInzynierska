import React from 'react';
import { Link } from 'react-router-dom';
import './MediaCard.css';
import defaultAvatar from '../assets/images/default-avatar.png';

function MediaCard({ linkTo, imageUrl, title, subtitle }) {
    return (
        <Link to={linkTo} className="media-card-link">
            <div className="media-card">
                <img
                    src={imageUrl || defaultAvatar}
                    alt={title}
                    className="media-card-image"
                />
                <div className="media-card-info">
                    <h3 className="media-card-title">{title}</h3>
                    <p className="media-card-subtitle">{subtitle}</p>
                </div>
            </div>
        </Link>
    );
}

export default MediaCard;