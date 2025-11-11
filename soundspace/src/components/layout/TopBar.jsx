import React from 'react'
import {Link} from 'react-router-dom'
import homepage from '../../assets/images/homepage.png'
import searchIcon from "../../assets/images/searchicon.png"
import './TopBar.css'
import {useAuth} from "../../context/AuthContext.jsx";
function TopBar() {
    const {openModal} = useAuth();
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

            <button className="login-button" onClick={() => openModal('login')}>
                Zaloguj
            </button>
            {/*<Link to="/login" className="login-button">*/}
            {/*    Zaloguj*/}
            {/*</Link>*/}
        </header>
    )
}
export default TopBar