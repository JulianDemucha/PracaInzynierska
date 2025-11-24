import React, { useState, useEffect } from 'react';
import { useAuth } from "../context/useAuth.js";
import { Link } from 'react-router-dom';
import './ProfilePage.css';
import defaultAvatar from '../assets/images/default-avatar.png';
import EditProfileModal from '../components/profile/EditProfileModal.jsx';
import AddSongModal from '../components/song/AddSongModal.jsx';
import CreateAlbumModal from '../components/album/CreateAlbumModal.jsx';
import MediaCard from '../components/cards/MediaCard.jsx';

import { getUserSongs, getCoverUrl } from '../services/songService.js';

// --- DANE TESTOWE DLA RESZTY ZAKŁADEK (Póki nie ma backendu dla albumów/playlist) ---
const mockOtherContent = {
    playlists: [
        { id: 1, title: "Moja playlista nr 1", year: 2022, coverArtUrl: "https://placehold.co/200x200/1DB954/white?text=Playlista+1", type: "Playlista" },
        { id: 2, title: "Moja playlista nr 2", year: 2021, coverArtUrl: "https://placehold.co/200x200/E73C7E/white?text=Playlista+2", type: "Playlista" },
    ],
    albums: [],
    comments: [
        { id: 1, text: "Mój komentarz 1..." }
    ]
};

function ProfilePage() {
    const { currentUser, logout, loading } = useAuth();

    const [activeTab, setActiveTab] = useState('wszystko');
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isAddSongModalOpen, setIsAddSongModalOpen] = useState(false);
    const [isCreateAlbumModalOpen, setIsCreateAlbumModalOpen] = useState(false);

    // Stan DANYCH z backendu
    const [userSongs, setUserSongs] = useState([]);

    // 1. POBIERANIE PIOSENEK Z BACKENDU
    useEffect(() => {
        const fetchMySongs = async () => {
            if (currentUser?.id) {
                try {
                    const data = await getUserSongs(currentUser.id);
                    setUserSongs(data);
                } catch (error) {
                    console.error("Błąd pobierania utworów użytkownika:", error);
                }
            }
        };

        fetchMySongs();
    }, [currentUser]);

    if (loading && !currentUser) {
        return (
            <div className="profile-page">
                <p>Ładowanie profilu…</p>
            </div>
        );
    }

    return (
        <div className="profile-page">

            {/* ===== NAGŁÓWEK ===== */}
            <header className="profile-header">
                <img
                    src={currentUser?.avatar || defaultAvatar}
                    alt="Awatar użytkownika"
                    className="profile-avatar"
                />
                <div className="profile-info">
                    <div className="profile-username-wrapper">
                        <h1 className="profile-username">{currentUser?.username || 'Nazwa Użytkownika'}</h1>
                    </div>
                    <p className="profile-bio">
                        {/* Tu można w przyszłości wyświetlać bio z bazy */}
                        Dołączył: {new Date().getFullYear()}
                    </p>
                </div>
            </header>

            {/* ===== NAWIGACJA ===== */}
            <nav className="profile-nav">
                <ul className="profile-tabs">
                    <li onClick={() => setActiveTab('wszystko')} className={activeTab === 'wszystko' ? 'active' : ''}>Wszystko</li>
                    <li onClick={() => setActiveTab('wlasne')} className={activeTab === 'wlasne' ? 'active' : ''}>Własne utwory</li>
                    <li onClick={() => setActiveTab('albumy')} className={activeTab === 'albumy' ? 'active' : ''}>Albumy</li>
                    <li onClick={() => setActiveTab('playlisty')} className={activeTab === 'playlisty' ? 'active' : ''}>Playlisty</li>
                    <li onClick={() => setActiveTab('komentarze')} className={activeTab === 'komentarze' ? 'active' : ''}>Komentarze</li>
                </ul>
                <div className="profile-nav-actions">
                    <button className="add-song-button" onClick={() => setIsAddSongModalOpen(true)}>
                        Dodaj utwór
                    </button>
                    <button className="add-album-button" onClick={() => setIsCreateAlbumModalOpen(true)}>
                        Dodaj album
                    </button>
                    <button className="edit-profile-button" onClick={() => setIsEditModalOpen(true)}>
                        Edytuj profil
                    </button>
                    <Link to="/" className="logout-button" onClick={logout}>
                        Wyloguj
                    </Link>
                </div>
            </nav>

            {/* ===== ZAWARTOŚĆ ===== */}
            <section className="profile-content">

                {/* --- Pokaż "Własne utwory" (Z BACKENDU) --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Własne utwory</h2>
                        <div className="media-grid">
                            {userSongs.length > 0 ? (
                                userSongs.map(song => (
                                    <MediaCard
                                        key={song.id}
                                        linkTo={`/song/${song.id}`}
                                        imageUrl={getCoverUrl(song.id)}
                                        title={song.title}
                                        subtitle={`${new Date(song.createdAt).getFullYear()} • Utwór`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Nie dodałeś jeszcze żadnych utworów.</p>
                            )}
                        </div>
                    </div>
                )}

                {/* --- Pokaż "Albumy" (MOCK) --- */}
                {(activeTab === 'wszystko' || activeTab === 'albumy') && (
                    <div className="content-section">
                        <h2>Albumy</h2>
                        <div className="media-grid">
                            {mockOtherContent.albums.length > 0 ? (
                                mockOtherContent.albums.map(album => (
                                    <MediaCard
                                        key={album.id}
                                        linkTo={`/album/${album.id}`}
                                        imageUrl={album.coverArtUrl}
                                        title={album.title}
                                        subtitle={`${album.year} • Album`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Nie dodałeś jeszcze żadnych albumów.</p>
                            )}
                        </div>
                    </div>
                )}

                {/* --- Pokaż "Playlisty" (MOCK) --- */}
                {(activeTab === 'wszystko' || activeTab === 'playlisty') && (
                    <div className="content-section">
                        <h2>Playlisty</h2>
                        <div className="media-grid">
                            {mockOtherContent.playlists.map(playlist => (
                                <MediaCard
                                    key={playlist.id}
                                    linkTo={`/playlist/${playlist.id}`}
                                    imageUrl={playlist.coverArtUrl}
                                    title={playlist.title}
                                    subtitle={playlist.type}
                                />
                            ))}
                        </div>
                    </div>
                )}

                {/* --- Pokaż "Komentarze" (MOCK) --- */}
                {(activeTab === 'wszystko' || activeTab === 'komentarze') && (
                    <div className="content-section">
                        <h2>Komentarze</h2>
                        <div className="song-row-placeholder">
                            {mockOtherContent.comments.length > 0
                                ? "Tutaj pojawią się Twoje komentarze (W budowie...)"
                                : "Brak komentarzy"}
                        </div>
                    </div>
                )}
            </section>

            {/* Modale */}
            <EditProfileModal
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)}
            />
            <AddSongModal
                isOpen={isAddSongModalOpen}
                onClose={() => setIsAddSongModalOpen(false)}
            />
            <CreateAlbumModal
                isOpen={isCreateAlbumModalOpen}
                onClose={() => setIsCreateAlbumModalOpen(false)}
            />
        </div>
    );
}

export default ProfilePage;