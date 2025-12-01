import React, { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from "../../context/useAuth.js";
import './Sidebar.css';

import { getUserAlbums } from '../../services/albumService.js';

// Importy Modali
import AddSongModal from '../song/AddSongModal.jsx';
import CreateAlbumModal from '../album/CreateAlbumModal.jsx';

const MOCK_PLAYLISTS = [
    { id: 101, name: "Do samochodu" },
    { id: 102, name: "Siłownia Pump" },
    { id: 103, name: "Sad Vibes 2024" },
];

function Sidebar() {
    const { currentUser, logout } = useAuth();

    const [userAlbums, setUserAlbums] = useState([]);
    const [userPlaylists, setUserPlaylists] = useState(MOCK_PLAYLISTS);

    // Stany widoczności modali
    const [isAddSongModalOpen, setIsAddSongModalOpen] = useState(false);
    const [isCreateAlbumModalOpen, setIsCreateAlbumModalOpen] = useState(false);

    // POBIERANIE DANYCH UŻYTKOWNIKA
    useEffect(() => {
        const fetchUserCollections = async () => {
            if (currentUser?.id) {
                try {
                    const albumsData = await getUserAlbums(currentUser.id);
                    setUserAlbums(albumsData);

                    // TODO: fetchowanie playlist
                    // const playlistsData = await getUserPlaylists(currentUser.id);
                    // setUserPlaylists(playlistsData);

                } catch (error) {
                    console.error("Błąd pobierania kolekcji użytkownika:", error);
                    setUserAlbums([]);
                }
            } else {
                setUserAlbums([]);
                setUserPlaylists(MOCK_PLAYLISTS);
            }
        };

        fetchUserCollections();
    }, [currentUser]);

    const allCollections = [
        ...userPlaylists,
        ...userAlbums.map(album => ({
            id: album.id,
            name: album.title,
            type: 'album'
        }))
    ];


    return (
        <aside className="sidebar">
            <div className="sidebar-content">

                {/* 1. SEKCJA: TWOJA BIBLIOTEKA */}
                <nav className="nav-section">
                    <p className="section-title">TWOJA BIBLIOTEKA</p>
                    <NavLink to="/favorites" className="nav-link">
                        Polubione
                    </NavLink>
                    <NavLink to={`/artist/${currentUser?.id}`} className="nav-link">
                        Moje Utwory
                    </NavLink>
                </nav>

                {/* 2. SEKCJA: PLAYLISTY I ALBUMY TWORCY */}
                <nav className="nav-section">
                    <p className="section-title">PLAYLISTY I ALBUMY</p>
                    <div className="playlists-list">
                        {allCollections.map((item, index) => (
                            <NavLink
                                key={`${item.type || 'playlist'}-${item.id}`}
                                to={item.type === 'album' ? `/album/${item.id}` : `/playlist/${item.id}`}
                                className="nav-link playlist-link"
                            >
                                # {item.name}
                            </NavLink>
                        ))}
                        {allCollections.length === 0 && currentUser && (
                            <span className="nav-link" style={{fontSize: '0.8rem', color: '#b3b3b3'}}>Brak kolekcji.</span>
                        )}
                        {allCollections.length === 0 && !currentUser && (
                            <span className="nav-link" style={{fontSize: '0.8rem', color: '#b3b3b3'}}>Zaloguj się, by zobaczyć kolekcje.</span>
                        )}
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
                    if (currentUser?.id) {
                        const fetchData = async () => {
                            const albumsData = await getUserAlbums(currentUser.id);
                            setUserAlbums(albumsData);
                        };
                        fetchData();
                    }
                }}
            />
        </aside>
    );
}

export default Sidebar;