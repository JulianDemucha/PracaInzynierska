import React, { useState } from 'react';
import {useAuth} from "../context/useAuth.js";
import { Link } from 'react-router-dom';
import './ProfilePage.css';
import defaultAvatar from '../assets/images/default-avatar.png';
import EditProfileModal from '../components/profile/EditProfileModal.jsx';
import AddSongModal from '../components/song/AddSongModal.jsx';
import CreateAlbumModal from '../components/album/CreateAlbumModal.jsx';
import MediaCard from '../components/cards/MediaCard.jsx';

// --- DANE TESTOWE ---
const mockUserContent = {
    songs: [
        { id: 1, title: "Utwór Kowala", year: 2024, coverArtUrl: "https://placehold.co/200x200/8A2BE2/white?text=Kowal", type: "Utwór" },
        { id: 2, title: "Mój utwór nr 2", year: 2023, coverArtUrl: "https://placehold.co/200x200/53346D/white?text=Utwor+2", type: "Utwór" },
    ],
    playlists: [
        { id: 1, title: "Moja playlista nr 1", year: 2022, coverArtUrl: "https://placehold.co/200x200/1DB954/white?text=Playlista+1", type: "Playlista" },
        { id: 2, title: "Moja playlista nr 2", year: 2021, coverArtUrl: "https://placehold.co/200x200/E73C7E/white?text=Playlista+2", type: "Playlista" },
    ],
    albums: [

    ],
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

    const content = mockUserContent;

    if (loading && !currentUser) {
        return (
            <div className="profile-page">
                <p>Ładowanie profilu…</p>
            </div>
        );
    }

    return (
        <div className="profile-page">

            {/* ===== 1. NAGŁÓWEK PROFILU (Zdjęcie, Nazwa, Bio) ===== */}
            <header className="profile-header">
                <img
                    src={currentUser.avatar || defaultAvatar}
                    alt="Awatar użytkownika"
                    className="profile-avatar"
                />
                <div className="profile-info">
                    <div className="profile-username-wrapper">
                        <h1 className="profile-username">{currentUser.username || 'Nazwa Użytkownika'}</h1>
                        {/* TODO 'currentUser.isVerified'  */}
                    </div>
                    <p className="profile-bio">
                        Nowy album 21.11.25 20:30
                    </p>
                </div>
            </header>

            {/* ===== 2. NAWIGACJA (Zakładki i Edycja) ===== */}
            <nav className="profile-nav">
                <ul className="profile-tabs">
                    <li onClick={() => setActiveTab('wszystko')} className={activeTab === 'wszystko' ? 'active' : ''}>Wszystko</li>
                    <li onClick={() => setActiveTab('wlasne')} className={activeTab === 'wlasne' ? 'active' : ''}>Własne utwory</li>
                    {/* ZMIANA: Dodano zakładkę "Albumy" */}
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

            {/* ===== 3. ZAWARTOŚĆ (ZMIENIONA NA SIATKĘ KART) ===== */}
            <section className="profile-content">

                {/* --- Pokaż "Własne utwory" --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Własne utwory</h2>
                        <div className="media-grid">
                            {content.songs.length > 0 ? (
                                content.songs.map(song => (
                                    <MediaCard
                                        key={song.id}
                                        linkTo={`/song/${song.id}`}
                                        imageUrl={song.coverArtUrl}
                                        title={song.title}
                                        subtitle={`${song.year} • ${song.type}`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Nie dodałeś jeszcze żadnych utworów.</p>
                            )}
                        </div>
                    </div>
                )}

                {/* --- Pokaż "Albumy" --- */}
                {(activeTab === 'wszystko' || activeTab === 'albumy') && (
                    <div className="content-section">
                        <h2>Albumy</h2>
                        <div className="media-grid">
                            {content.albums.length > 0 ? (
                                content.albums.map(album => (
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

                {/* --- Pokaż "Playlisty" --- */}
                {(activeTab === 'wszystko' || activeTab === 'playlisty') && (
                    <div className="content-section">
                        <h2>Playlisty</h2>
                        <div className="media-grid">
                            {content.playlists.map(playlist => (
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

                {/* --- Pokaż "Komentarze"  --- */}
                {(activeTab === 'wszystko' || activeTab === 'komentarze') && (
                    <div className="content-section">
                        <h2>Komentarze</h2>
                        <div className="song-row-placeholder">Mój komentarz 1...</div>
                    </div>
                )}
            </section>

            {/* Modale (bez zmian) */}
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