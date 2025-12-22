import React, { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from "../../context/useAuth.js";
import './SideBar.css';
import { getUserAlbums } from '../../services/albumService.js';
import { getUserPlaylists } from '../../services/playlistService.js';
import AddSongModal from '../song/AddSongModal.jsx';
import CreateAlbumModal from '../album/CreateAlbumModal.jsx';

function Sidebar() {
    const { currentUser, logout } = useAuth();
    const [userAlbums, setUserAlbums] = useState([]);
    const [userPlaylists, setUserPlaylists] = useState([]);
    const [isAddSongModalOpen, setIsAddSongModalOpen] = useState(false);
    const [isCreateAlbumModalOpen, setIsCreateAlbumModalOpen] = useState(false);

    useEffect(() => {
        const fetchUserCollections = async () => {
            if (currentUser?.id) {
                try {
                    const [albumsData, playlistsData] = await Promise.all([
                        getUserAlbums(currentUser.id),
                        getUserPlaylists(currentUser.id)
                    ]);

                    setUserAlbums(albumsData || []);
                    setUserPlaylists(playlistsData || []);

                } catch (error) {
                    console.error("Błąd pobierania kolekcji użytkownika:", error);
                    setUserAlbums([]);
                    setUserPlaylists([]);
                }
            } else {
                setUserAlbums([]);
                setUserPlaylists([]);
            }
        };

        fetchUserCollections();
    }, [currentUser]);

    return (
        <aside className="sidebar">
            <div className="sidebar-content">
                <nav className="nav-section">
                    <p className="section-title">TWOJA BIBLIOTEKA</p>
                    <NavLink to="/favorites" className="nav-link">
                        Polubione
                    </NavLink>
                    <NavLink to={`/artist/${currentUser?.id}`} className="nav-link">
                        Moje Utwory
                    </NavLink>
                </nav>

                <nav className="nav-section">
                    <p className="section-title">TWOJE PLAYLISTY</p>
                    <div className="playlists-list">
                        {userPlaylists.map((playlist) => (
                            <NavLink
                                key={`playlist-${playlist.id}`}
                                to={`/playlist/${playlist.id}`}
                                className="nav-link playlist-link"
                            >
                                # {playlist.title}
                            </NavLink>
                        ))}

                        {userPlaylists.length === 0 && currentUser && (
                            <span className="nav-link playlist-empty-msg">
                                Brak playlist.
                            </span>
                        )}
                        {!currentUser && (
                            <span className="nav-link playlist-empty-msg">
                                Zaloguj się.
                            </span>
                        )}
                    </div>
                </nav>

                <nav className="nav-section">
                    <p className="section-title">TWOJE ALBUMY</p>
                    <div className="playlists-list">
                        {userAlbums.map((album) => (
                            <NavLink
                                key={`album-${album.id}`}
                                to={`/album/${album.id}`}
                                className="nav-link playlist-link"
                            >
                                # {album.title}
                            </NavLink>
                        ))}

                        {userAlbums.length === 0 && currentUser && (
                            <span className="nav-link playlist-empty-msg">
                                Brak albumów.
                            </span>
                        )}
                        {!currentUser && (
                            <span className="nav-link playlist-empty-msg">
                                Zaloguj się.
                            </span>
                        )}
                    </div>
                </nav>
            </div>

            <div className="sidebar-footer">
                <div className="cta-container">
                    <button
                        className="upload-btn-large"
                        onClick={() => setIsAddSongModalOpen(true)}
                        disabled={!currentUser}
                        title={!currentUser ? "Zaloguj się, aby dodać utwór" : ""}
                    >
                        Dodaj Utwór
                    </button>

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
                                <span className="user-role">Użytkownik</span>
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