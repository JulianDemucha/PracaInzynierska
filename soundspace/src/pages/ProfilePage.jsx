import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext'; // Użyjemy go do pobrania danych usera

// Importujemy style (stworzymy je w Kroku 2)
import './ProfilePage.css';

// Importujemy placeholder awatara (ten sam co w TopBar)
import defaultAvatar from '../assets/images/default-avatar.png';

function ProfilePage() {
    // Pobieramy zalogowanego użytkownika (na razie naszego "na niby")
    const { currentUser, logout } = useAuth();

    // Stan do zarządzania aktywną zakładką
    const [activeTab, setActiveTab] = useState('wszystko');

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
                    <span className="profile-type">Użytkownik</span>
                    <h1 className="profile-username">{currentUser.name || 'Nazwa Użytkownika'}</h1>
                    <p className="profile-bio">
                        Tu będzie jakieś info o użytkowniku / bio.
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
                <button className="edit-profile-button" onClick={logout}>
                    Wyloguj (test)
                </button>
            </nav>

            {/* ===== 3. ZAWARTOŚĆ (Listy) ===== */}
            <section className="profile-content">

                {/* --- Pokaż "Wszystko" --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Własne utwory</h2>
                        {/* Tu w przyszłości będzie .map() po utworach */}
                        <div className="song-row-placeholder">Mój utwór nr 1</div>
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
        </div>
    );
}

export default ProfilePage;