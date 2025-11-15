import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import './ArtistPage.css';
import defaultAvatar from '../assets/images/default-avatar.png';
import verified from '../assets/images/check.png';


const mockArtistDatabase = {
    "123": {
        id: 123,
        username: "Artysta Testowy",
        avatar: defaultAvatar,
        bio: "Witaj na moim profilu. Sprawdź moje najnowsze utwory i albumy. Już wkrótce nowa płyta!",
        isVerified: true,
        songs: [
            { id: 1, title: "Mój publiczny utwór nr 1", visibility: "PUBLIC" },
            { id: 2, title: "Mój prywatny utwór (nie widać)", visibility: "PRIVATE" },
            { id: 3, title: "Kolejny hit (publiczny)", visibility: "PUBLIC" },
        ],
        playlists: [
            { id: 1, title: "Playlista publiczna", visibility: "PUBLIC" },
            { id: 2, title: "Playlista prywatna (nie widać)", visibility: "PRIVATE" },
        ],
        albums: [
            // Pusta tablica albumów (jeszcze nie dodano)
        ]
    }
};
function ArtistPage() {
    const { id } = useParams();

    // --- (symulacja) ---
    const artist = mockArtistDatabase[id] || null;

    // Stan do zarządzania aktywną zakładką
    const [activeTab, setActiveTab] = useState('wszystko');

    // Obsługa ładowania lub gdy artysta nie istnieje
    if (!artist) {
        return <div className="profile-page">Ładowanie profilu artysty... lub artysta nie istnieje.</div>;
    }

    // Filtrujemy treści, aby pokazać tylko publiczne
    const publicSongs = artist.songs.filter(song => song.visibility === 'PUBLIC');
    const publicPlaylists = artist.playlists.filter(playlist => playlist.visibility === 'PUBLIC');
    const publicAlbums = artist.albums.filter(album => album.visibility === 'PUBLIC');

    return (
        <div className="profile-page">

            {/* ===== 1. NAGŁÓWEK PROFILU ===== */}
            <header className="profile-header">
                <img
                    src={artist.avatar}
                    alt="Awatar artysty"
                    className="profile-avatar"
                />
                <div className="profile-info">
                    <div className="profile-username-wrapper">
                        <h1 className="profile-username">{artist.username}</h1>
                        {artist.isVerified && (
                            <span className="verified-badge" title="Zweryfikowany artysta">
                                <img src={verified} alt="Verified" className="verified-artist"/>
                            </span>
                        )}
                    </div>
                    {/* Wyświetlanie bio artysty */}
                    <p className="profile-bio">
                        {artist.bio}
                    </p>
                </div>
            </header>

            {/* ===== 2. NAWIGACJA (Wersja publiczna, bez przycisków) ===== */}
            <nav className="profile-nav">
                <ul className="profile-tabs">
                    <li onClick={() => setActiveTab('wszystko')} className={activeTab === 'wszystko' ? 'active' : ''}>Wszystko</li>
                    <li onClick={() => setActiveTab('wlasne')} className={activeTab === 'wlasne' ? 'active' : ''}>Własne utwory</li>
                    <li onClick={() => setActiveTab('albumy')} className={activeTab === 'albumy' ? 'active' : ''}>Albumy</li>
                    <li onClick={() => setActiveTab('playlisty')} className={activeTab === 'playlisty' ? 'active' : ''}>Playlisty</li>
                </ul>
            </nav>

            {/* ===== 3. ZAWARTOŚĆ ===== */}
            <section className="profile-content">

                {/* --- Pokaż "Własne utwory" --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Własne utwory</h2>
                        {publicSongs.length > 0 ? (
                            publicSongs.map(song => (
                                <div key={song.id} className="song-row-placeholder">
                                    {song.title}
                                </div>
                            ))
                        ) : (
                            <p className="empty-tab-message">Ten artysta nie udostępnił jeszcze żadnych utworów.</p>
                        )}
                    </div>
                )}

                {/* --- Pokaż "Albumy" --- */}
                {(activeTab === 'wszystko' || activeTab === 'albumy') && (
                    <div className="content-section">
                        <h2>Albumy</h2>
                        {publicAlbums.length > 0 ? (
                            publicAlbums.map(album => (
                                <div key={album.id} className="song-row-placeholder">
                                    {album.title}
                                </div>
                            ))
                        ) : (
                            <p className="empty-tab-message">Ten artysta nie udostępnił jeszcze żadnych albumów.</p>
                        )}
                    </div>
                )}

                {/* --- Pokaż "Playlisty" --- */}
                {(activeTab === 'wszystko' || activeTab === 'playlisty') && (
                    <div className="content-section">
                        <h2>Playlisty</h2>
                        {publicPlaylists.length > 0 ? (
                            publicPlaylists.map(playlist => (
                                <div key={playlist.id} className="song-row-placeholder">
                                    {playlist.title}
                                </div>
                            ))
                        ) : (
                            <p className="empty-tab-message">Ten artysta nie udostępnił jeszcze żadnych playlist.</p>
                        )}
                    </div>
                )}
            </section>
        </div>
    );
}

export default ArtistPage;