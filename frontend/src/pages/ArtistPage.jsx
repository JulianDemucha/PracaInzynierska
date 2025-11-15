import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import './ArtistPage.css';
import defaultAvatar from '../assets/images/default-avatar.png';
import verifiedBadge from '../assets/images/verified.png';
import MediaCard from '../cards/MediaCard.jsx';

// --- 2.  DANE TESTOWE ---
const mockArtistDatabase = {
    "123": {
        id: 123,
        username: "Artysta Testowy",
        avatar: defaultAvatar,
        bio: "Witaj na moim profilu. Sprawdź moje najnowsze utwory i albumy. Już wkrótce nowa płyta!",
        isVerified: true,
        songs: [
            { id: 1, title: "Mój publiczny utwór nr 1", visibility: "PUBLIC", year: 2024, coverArtUrl: "https://placehold.co/200x200/8A2BE2/white?text=Utwor+1" },
            { id: 2, title: "Mój prywatny utwór (nie widać)", visibility: "PUBLIC", year: 2024, coverArtUrl: null },
            { id: 3, title: "Kolejny hit (publiczny)", visibility: "PUBLIC", year: 2023, coverArtUrl: "https://placehold.co/200x200/53346D/white?text=Hit" },
        ],
        playlists: [
            { id: 1, title: "Playlista publiczna", visibility: "PUBLIC", year: 2022, coverArtUrl: "https://placehold.co/200x200/1DB954/white?text=Playlista" },
            { id: 2, title: "Playlista prywatna (nie widać)", visibility: "PRIVATE", year: 2021, coverArtUrl: null },
        ],
        albums: [
            { id: 1, title: "Nazwa Albumu", visibility: "PUBLIC", year: 2025, coverArtUrl: "https://placehold.co/200x200/E73C7E/white?text=Album" },
        ]
    }
};

function ArtistPage() {
    const { id } = useParams();
    const artist = mockArtistDatabase[id] || null;
    const [activeTab, setActiveTab] = useState('wszystko');

    if (!artist) {
        return <div className="profile-page">Ładowanie profilu artysty... lub artysta nie istnieje.</div>;
    }

    const publicSongs = artist.songs.filter(song => song.visibility === 'PUBLIC');
    const publicPlaylists = artist.playlists.filter(playlist => playlist.visibility === 'PUBLIC');
    const publicAlbums = artist.albums.filter(album => album.visibility === 'PUBLIC');

    return (
        <div className="profile-page">

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
                            <img src={verifiedBadge} alt="Zweryfikowany" className="verified-badge" title="Zweryfikowany artysta" />
                        )}
                    </div>
                    <p className="profile-bio">
                        {artist.bio}
                    </p>
                </div>
            </header>

            <nav className="profile-nav">
                <ul className="profile-tabs">
                    <li onClick={() => setActiveTab('wszystko')} className={activeTab === 'wszystko' ? 'active' : ''}>Wszystko</li>
                    <li onClick={() => setActiveTab('wlasne')} className={activeTab === 'wlasne' ? 'active' : ''}>Własne utwory</li>
                    <li onClick={() => setActiveTab('albumy')} className={activeTab === 'albumy' ? 'active' : ''}>Albumy</li>
                    <li onClick={() => setActiveTab('playlisty')} className={activeTab === 'playlisty' ? 'active' : ''}>Playlisty</li>
                </ul>
                <div className="profile-nav-actions-placeholder"></div>
            </nav>

            {/* ===== 3. ZAWARTOŚĆ (ZMIENIONA NA SIATKĘ KART) ===== */}
            <section className="profile-content">

                {/* --- Pokaż "Własne utwory" --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Własne utwory</h2>
                        <div className="media-grid">
                            {publicSongs.length > 0 ? (
                                publicSongs.map(song => (
                                    <MediaCard
                                        key={song.id}
                                        linkTo={`/song/${song.id}`}
                                        imageUrl={song.coverArtUrl || defaultAvatar}
                                        title={song.title}
                                        subtitle={`${song.year} • Utwór`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Ten artysta nie udostępnił jeszcze żadnych utworów.</p>
                            )}
                        </div>
                    </div>
                )}

                {/* --- Pokaż "Albumy" --- */}
                {(activeTab === 'wszystko' || activeTab === 'albumy') && (
                    <div className="content-section">
                        <h2>Albumy</h2>
                        <div className="media-grid">
                            {publicAlbums.length > 0 ? (
                                publicAlbums.map(album => (
                                    <MediaCard
                                        key={album.id}
                                        linkTo={`/album/${album.id}`}
                                        imageUrl={album.coverArtUrl || defaultAvatar}
                                        title={album.title}
                                        subtitle={`${album.year} • Album`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Ten artysta nie udostępnił jeszcze żadnych albumów.</p>
                            )}
                        </div>
                    </div>
                )}

                {/* --- Pokaż "Playlisty" --- */}
                {(activeTab === 'wszystko' || activeTab === 'playlisty') && (
                    <div className="content-section">
                        <h2>Playlisty</h2>
                        <div className="media-grid">
                            {publicPlaylists.length > 0 ? (
                                publicPlaylists.map(playlist => (
                                    <MediaCard
                                        key={playlist.id}
                                        linkTo={`/playlist/${playlist.id}`}
                                        imageUrl={playlist.coverArtUrl || defaultAvatar}
                                        title={playlist.title}
                                        subtitle={`• Playlista`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Ten artysta nie udostępnił jeszcze żadnych playlist.</p>
                            )}
                        </div>
                    </div>
                )}
            </section>
        </div>
    );
}

export default ArtistPage;