import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import './ArtistPage.css';
import defaultAvatar from '../assets/images/default-avatar.png';
import verifiedBadge from '../assets/images/verified.png';
import MediaCard from '../components/cards/MediaCard.jsx';
import api from '../context/axiosClient.js';
import {getImageUrl} from '../services/imageService.js';

const getYearFromDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).getFullYear();
};

const translateSex = (sex) => {
    if (sex === 'MALE') return 'Mężczyzna';
    if (sex === 'FEMALE') return 'Kobieta';
    return 'Inna';
};

function ArtistPage() {
    const {id} = useParams()
    const [artist, setArtist] = useState(null);
    const [songs, setSongs] = useState([]);
    const [albums, setAlbums] = useState([]);
    const [playlists, setPlaylists] = useState([]);

    const [activeTab, setActiveTab] = useState('wszystko');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                // Pobieranie danych artysty, piosenek i albumów
                const [userRes, songsRes, albumsRes] = await Promise.all([
                    api.get(`/users/${id}`),
                    api.get(`/songs/user/${id}`),
                    api.get(`/albums/user/${id}`)
                ]);

                setArtist(userRes.data);
                setSongs(songsRes.data);
                setAlbums(albumsRes.data);

                // TODO: fetchowanie playlist, gdy backend będzie gotowy

            } catch (err) {
                console.error("Błąd pobierania danych profilu:", err);
                setError("Nie udało się załadować profilu artysty.");
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            fetchData();
        }
    }, [id]);

    if(loading){
        return <div className="profile-page" style={{padding: '2rem'}}>Ładowanie profilu...</div>;
    }

    if (error || !artist) {
        return <div className="profile-page" style={{padding: '2rem'}}>{error || "Nie znaleziono artysty."}</div>;
    }

    return (
        <div className="profile-page">

            <header className="profile-header">
                <img
                    // Używamy helpera do pobrania zdjęcia
                    src={getImageUrl(artist.avatarStorageKeyId ?? artist.avatarId)}
                    alt="Awatar artysty"
                    className="profile-avatar"
                    onError={(e) => {e.target.src = defaultAvatar}}
                />
                <div className="profile-info">
                    <div className="profile-username-wrapper">
                        <h1 className="profile-username">{artist.username}</h1>
                        {artist.emailVerified && (
                            <img src={verifiedBadge} alt="Zweryfikowany" className="verified-badge"
                                 title="Zweryfikowany artysta"/>
                        )}
                    </div>

                    <p className="profile-bio">
                        {artist.bio || "Brak bio."}
                    </p>

                    <div className="profile-meta-details">
                        <span className="meta-item">
                            Dołączył: {getYearFromDate(artist.createdAt)}
                        </span>
                        {artist.sex && artist.sex !== 'OTHER' && (
                            <>
                                <span className="meta-separator">•</span>
                                <span className="meta-item">
                                    {translateSex(artist.sex)}
                                </span>
                            </>
                        )}
                    </div>
                </div>
            </header>

            <nav className="profile-nav">
                <ul className="profile-tabs">
                    <li onClick={() => setActiveTab('wszystko')}
                        className={activeTab === 'wszystko' ? 'active' : ''}>Wszystko
                    </li>
                    <li onClick={() => setActiveTab('wlasne')} className={activeTab === 'wlasne' ? 'active' : ''}>Własne
                        utwory
                    </li>
                    <li onClick={() => setActiveTab('albumy')}
                        className={activeTab === 'albumy' ? 'active' : ''}>Albumy
                    </li>
                    <li onClick={() => setActiveTab('playlisty')}
                        className={activeTab === 'playlisty' ? 'active' : ''}>Playlisty
                    </li>
                </ul>
                <div className="profile-nav-actions-placeholder"></div>
            </nav>

            {/* ===== ZAWARTOŚĆ ===== */}
            <section className="profile-content">

                {/* --- Pokaż "Utwory" --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Utwory</h2>
                        <div className="media-grid">
                            {songs && songs.length > 0 ? (
                                songs.map(song => (
                                    <MediaCard
                                        key={song.id}
                                        linkTo={`/song/${song.id}`}
                                        imageUrl={getImageUrl(song.coverStorageKeyId)}
                                        title={song.title}
                                        subtitle={`${getYearFromDate(song.createdAt)} • Utwór`}
                                        // NAPRAWA: Dodano prop data={song}, aby przycisk Play się pojawił
                                        data={song}
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
                            {albums && albums.length > 0 ? (
                                albums.map(album => (
                                    <MediaCard
                                        key={album.id}
                                        linkTo={`/album/${album.id}`}
                                        imageUrl={getImageUrl(album.coverStorageKeyId)}
                                        title={album.title}
                                        subtitle={`${getYearFromDate(album.createdAt)} • Album`}
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
                            {playlists && playlists.length > 0 ? (
                                playlists.map(playlist => (
                                    <MediaCard
                                        key={playlist.id}
                                        linkTo={`/playlist/${playlist.id}`}
                                        imageUrl={defaultAvatar}
                                        title={playlist.title}
                                        subtitle={`• Playlista`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Brak publicznych playlist.</p>
                            )}
                        </div>
                    </div>
                )}
            </section>
        </div>
    );
}

export default ArtistPage;