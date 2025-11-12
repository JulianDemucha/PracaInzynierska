import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import './ProfilePage.css';
import defaultAvatar from '../assets/images/default-avatar.png';
import EditProfileModal from '../components/profile/EditProfileModal.jsx';
import AddSongModal from '../components/song/AddSongModal.jsx';

function ProfilePage() {
    const { currentUser, logout } = useAuth();
    const [activeTab, setActiveTab] = useState('wszystko');
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isAddSongModalOpen, setIsAddSongModalOpen] = useState(false);
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
                    <h1 className="profile-username">{currentUser.name || 'Nazwa Użytkownika'}</h1>
                    <p className="profile-bio">
                        Nowy album 21.11.25 20:30
                    </p>
                </div>
            </header>

            {/* ===== 2. NAWIGACJA (Zakładki i Edycja) ===== */}
            <nav className="profile-nav">
                <ul className="profile-tabs">
                    {/* Przyciski aktualizują stan 'activeTab' po kliknięciu */}
                    <li onClick={() => setActiveTab('wszystko')} className={activeTab === 'wszystko' ? 'active' : ''}>Wszystko</li>
                    <li onClick={() => setActiveTab('wlasne')} className={activeTab === 'wlasne' ? 'active' : ''}>Własne utwory</li>
                    <li onClick={() => setActiveTab('playlisty')} className={activeTab === 'playlisty' ? 'active' : ''}>Playlisty</li>
                    <li onClick={() => setActiveTab('komentarze')} className={activeTab === 'komentarze' ? 'active' : ''}>Komentarze</li>
                </ul>
                <div className="profile-nav-actions">
                    <button className="add-song-button" onClick={() => setIsAddSongModalOpen(true)}>
                        Dodaj utwór
                    </button>
                    <button className="edit-profile-button" onClick={() => setIsEditModalOpen(true)}>
                        Edytuj profil
                    </button>
                    <Link to="/" className="logout-button" onClick={logout}>
                        Wyloguj
                    </Link>
                </div>
            </nav>

            {/* ===== 3. ZAWARTOŚĆ (Listy) ===== */}
            <section className="profile-content">

                {/* --- Pokaż "Wszystko" --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Własne utwory</h2>
                        {/* Tu w przyszłości będzie .map() po utworach */}
                        <div className="song-row-placeholder">Utwór Kowala</div>
                        <div className="song-row-placeholder">Mój utwór nr 2</div>
                    </div>
                )}

                {/* --- Pokaż "Playlisty" --- */}
                {(activeTab === 'wszystko' || activeTab === 'playlisty') && (
                    <div className="content-section">
                        <h2>Playlisty</h2>
                        <div className="song-row-placeholder">Moja playlista nr 1</div>
                        <div className="song-row-placeholder">Moja playlista nr 2</div>
                        <div className="song-row-placeholder">Moja playlista nr 1</div>
                        <div className="song-row-placeholder">Moja playlista nr 2</div>
                        <div className="song-row-placeholder">Moja playlista nr 1</div>
                        <div className="song-row-placeholder">Moja playlista nr 2</div>
                        <div className="song-row-placeholder">Moja playlista nr 1</div>
                        <div className="song-row-placeholder">Moja playlista nr 2</div>
                        <div className="song-row-placeholder">Moja playlista nr 1</div>
                        <div className="song-row-placeholder">Moja playlista nr 2</div>
                        <div className="song-row-placeholder">Moja playlista nr 1</div>
                        <div className="song-row-placeholder">Moja playlista nr 2</div>
                        <div className="song-row-placeholder">Moja playlista nr 1</div>
                        <div className="song-row-placeholder">Moja playlista nr 2</div>
                        <div className="song-row-placeholder">Moja playlista nr 1</div>
                        <div className="song-row-placeholder">Moja playlista nr 2</div>

                    </div>
                )}

                {/* --- Pokaż "Komentarze" --- */}
                {(activeTab === 'wszystko' || activeTab === 'komentarze') && (
                    <div className="content-section">
                        <h2>Komentarze</h2>
                        <div className="song-row-placeholder">Mój komentarz 1...</div>
                    </div>
                )}
            </section>
            <EditProfileModal
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)}
            />
            <AddSongModal
                isOpen={isAddSongModalOpen}
                onClose={() => setIsAddSongModalOpen(false)}
            />
        </div>
    );
}

export default ProfilePage;