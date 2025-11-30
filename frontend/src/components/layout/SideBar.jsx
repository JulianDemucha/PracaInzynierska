import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from "../../context/useAuth.js";
import './Sidebar.css';

// Importy Modali
import AddSongModal from '../song/AddSongModal.jsx';
import CreateAlbumModal from '../album/CreateAlbumModal.jsx';

function Sidebar() {
    const { currentUser, logout } = useAuth();

    // Stany widoczności modali
    const [isAddSongModalOpen, setIsAddSongModalOpen] = useState(false);
    const [isCreateAlbumModalOpen, setIsCreateAlbumModalOpen] = useState(false);

    const [playlists, setPlaylists] = useState([
        { id: 1, name: "Do samochodu" },
        { id: 2, name: "Siłownia Pump" },
        { id: 3, name: "Sad Vibes 2024" },
        { id: 1, name: "Do samochodu" },
        { id: 2, name: "Siłownia Pump" },
        { id: 1, name: "Do samochodu" },
        { id: 2, name: "Siłownia Pump" },
        { id: 1, name: "Do samochodu" },
        { id: 2, name: "Siłownia Pump" },
        { id: 1, name: "Do samochodu" },
        { id: 2, name: "Siłownia Pump" },
        { id: 1, name: "Do samochodu" },
        { id: 2, name: "Siłownia Pump" },
        { id: 1, name: "Do samochodu" },
        { id: 2, name: "Siłownia Pump" },
        { id: 1, name: "Do samochodu" },
        { id: 2, name: "Siłownia Pump" },
    ]);

    return (
        <aside className="sidebar">
            <div className="sidebar-content">

                {/* 1. SEKCJA: TWOJA BIBLIOTEKA */}
                <nav className="nav-section">
                    <p className="section-title">TWOJA BIBLIOTEKA</p>
                    <NavLink to="/favorites" className="nav-link">
                        Polubione
                    </NavLink>
                    <NavLink to="/my-songs" className="nav-link">
                        Moje Utwory
                    </NavLink>
                </nav>

                {/* 2. SEKCJA: PLAYLISTY */}
                <nav className="nav-section">
                    <p className="section-title">PLAYLISTY</p>
                    <div className="playlists-list">
                        {playlists.map((playlist, index) => (
                            <NavLink
                                key={`${playlist.id}-${index}`} // Poprawiono klucz dla powtarzających się ID
                                to={`/playlist/${playlist.id}`}
                                className="nav-link playlist-link"
                            >
                                # {playlist.name}
                            </NavLink>
                        ))}
                    </div>
                </nav>
            </div>

            {/* 3. STOPKA */}
            <div className="sidebar-footer">
                <div className="cta-container">
                    {/* PRZYCISK: DODAJ UTWÓR */}
                    <button
                        className="upload-btn-large"
                        onClick={() => setIsAddSongModalOpen(true)}
                        disabled={!currentUser}
                        title={!currentUser ? "Zaloguj się, aby dodać utwór" : ""}
                    >
                        Dodaj Utwór
                    </button>

                    {/* PRZYCISK: WYDAJ ALBUM */}
                    <button
                        className="upload-btn-large album-btn"
                        onClick={() => setIsCreateAlbumModalOpen(true)}
                        disabled={!currentUser}
                        title={!currentUser ? "Zaloguj się, aby stworzyć album" : ""}
                    >
                        Wydaj Album
                    </button>
                </div>

                {currentUser ? (
                    <div className="user-profile-container">
                        <div className="user-profile">
                            <div className="avatar-circle">
                                {currentUser.username ? currentUser.username.charAt(0).toUpperCase() : 'U'}
                            </div>
                            <div className="user-details">
                                <span className="username">{currentUser.username}</span>
                                <span className="user-role">Artysta</span>
                            </div>
                        </div>

                        <div className="user-controls-text">
                            <button onClick={logout} className="text-control logout">Wyloguj</button>
                        </div>
                    </div>
                ) : (
                    <div className="login-prompt">
                        <p>Zaloguj się, aby zarządzać muzyką.</p>
                    </div>
                )}
            </div>

            {/* MODALE */}
            <AddSongModal
                isOpen={isAddSongModalOpen}
                onClose={() => setIsAddSongModalOpen(false)}
            />
            <CreateAlbumModal
                isOpen={isCreateAlbumModalOpen}
                onClose={() => {
                    setIsCreateAlbumModalOpen(false);
                }}
            />
        </aside>
    );
}

export default Sidebar;