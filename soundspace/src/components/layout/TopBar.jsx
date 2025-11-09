import React from 'react'
import {Link} from 'react-router-dom'
import homepage from '../../assets/images/homepage.png'
import searchIcon from "../../assets/images/searchicon.png"
import './TopBar.css'
import ContextMenu from '../common/ContextMenu.jsx';
function TopBar() {
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

            <Link to="/login" className="login-button">
                Zaloguj
            </Link>
        </header>
    )
}
export default TopBar