import React from 'react'
import {Link} from 'react-router-dom'
import homepage from '../../assets/images/homepage.png'
import searchIcon from "../../assets/images/searchicon.png"
import './TopBar.css'
import {useAuth} from "../../context/useAuth.js";
import {getImageUrl} from "../../services/imageService.js";
import defaultAvatar from "../../assets/images/default-avatar.png";

function TopBar() {
    const { currentUser, openModal } = useAuth();
    return (
        <header className="topbar">
            <div className="topbar-homepage-icon">
                <Link to="/">
                    <img src={homepage} alt="Homepage Logo" />
                </Link>
            </div>
            <div className="topbar-search">
                <img src={searchIcon} alt="Wyszukaj" className="topbar-search-icon" />
                <input type="text" placeholder="Wyszukaj..."/>
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
export default TopBar