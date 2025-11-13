import * as React from "react";
import {Link} from "react-router-dom";
import './SideBar.css'
function SideBar() {
    return (
        <nav className="sidebar">
            <ul className="nav">
                <li className="nav-item">
                    <Link className="nav-link" to="/">
                        Strona Główna
                    </Link>
                </li>
                <li className="nav-item">
                    <Link className="nav-link" to="/profile">
                        Profil Użytkownika
                    </Link>
                </li>
            </ul>
        </nav>
    )
}
export default SideBar