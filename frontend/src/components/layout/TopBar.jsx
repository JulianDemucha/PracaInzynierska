import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import homepage from '../../assets/images/homepage.png';
import searchIcon from "../../assets/images/searchicon.png";
import './TopBar.css';
import { useAuth } from "../../context/useAuth.js";
import { getImageUrl } from "../../services/imageService.js";
import defaultAvatar from "../../assets/images/default-avatar.png";

function TopBar() {
    const { currentUser, openModal } = useAuth();
    const [searchTerm, setSearchTerm] = useState("");
    const navigate = useNavigate();

    const handleSearch = () => {
        if (searchTerm.trim()) {
            navigate(`/search?q=${encodeURIComponent(searchTerm)}`);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    };

    return (
        <header className="topbar">
            <div className="topbar-homepage-icon">
                <Link to="/">
                    <img src={homepage} alt="Homepage Logo" />
                </Link>
            </div>
            <div className="topbar-search">
                <img
                    src={searchIcon}
                    alt="Wyszukaj"
                    className="topbar-search-icon"
                    onClick={handleSearch}
                    style={{cursor: 'pointer'}}
                />
                <input
                    type="text"
                    placeholder="Wyszukaj..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onKeyDown={handleKeyDown}
                />
            </div>

            <div className="topbar-profile">
                {currentUser ? (
                    <Link to="/profile">
                        <img
                            src={getImageUrl(currentUser.avatarStorageKeyId) || defaultAvatar}
                            alt="Mój profil"
                            className="topbar-avatar"
                        />
                    </Link>
                ) : (
                    <button className="login-button" onClick={() => openModal('login')}>
                        Zaloguj
                    </button>
                )}
            </div>
        </header>
    )
}

export default TopBar;