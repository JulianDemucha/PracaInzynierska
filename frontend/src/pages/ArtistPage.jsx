import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import MediaCard from '../components/cards/MediaCard.jsx';
import api from '../context/axiosClient.js';
import { getImageUrl } from '../services/imageService.js';
import defaultAvatar from '../assets/images/default-avatar.png';
import verifiedBadge from '../assets/images/verified.png';
import './ArtistPage.css';

const getYearFromDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).getFullYear();
};

const translateSex = (sex) => {
    if (sex === 'MALE') return 'Mężczyzna';
    if (sex === 'FEMALE') return 'Kobieta';
    return 'Inna';
};

const ITEMS_PER_ROW = 7;
const SONGS_INITIAL_LIMIT = ITEMS_PER_ROW * 2;
const OTHERS_INITIAL_LIMIT = ITEMS_PER_ROW;

const ExpandControls = ({ totalCount, currentLimit, initialLimit, onUpdate }) => {
    if (totalCount <= initialLimit) return null;

    return (
        <div className="expand-controls">
            {currentLimit < totalCount && (
                <>
                    <button
                        className="expand-btn"
                        onClick={() => onUpdate(Math.min(currentLimit + ITEMS_PER_ROW, totalCount))}
                    >
                        Pokaż 7 więcej
                    </button>
                    <button
                        className="expand-btn"
                        onClick={() => onUpdate(totalCount)}
                    >
                        Pokaż wszystkie
                    </button>
                </>
            )}

            {currentLimit > initialLimit && (
                <>
                    <button
                        className="expand-btn"
                        onClick={() => onUpdate(Math.max(currentLimit - ITEMS_PER_ROW, initialLimit))}
                    >
                        Pokaż 7 mniej
                    </button>
                    <button
                        className="expand-btn"
                        onClick={() => onUpdate(initialLimit)}
                    >
                        Zwiń
                    </button>
                </>
            )}
        </div>
    );
};

function ArtistPage() {
    const { id } = useParams();
    const [artist, setArtist] = useState(null);
    const [songs, setSongs] = useState([]);
    const [albums, setAlbums] = useState([]);
    const [playlists, setPlaylists] = useState([]);

    const [activeTab, setActiveTab] = useState('wszystko');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [songsLimit, setSongsLimit] = useState(SONGS_INITIAL_LIMIT);
    const [albumsLimit, setAlbumsLimit] = useState(OTHERS_INITIAL_LIMIT);
    const [playlistsLimit, setPlaylistsLimit] = useState(OTHERS_INITIAL_LIMIT);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                const userRes = await api.get(`/users/${id}`);
                setArtist(userRes.data);

                const results = await Promise.allSettled([
                    api.get(`/songs/user/${id}`),
                    api.get(`/albums/user/${id}`),
                    api.get(`/playlists/user/${id}`)
                ]);

                const getData = (result) => {
                    if (result.status === 'fulfilled') {
                        return result.value.data || [];
                    } else {
                        return [];
                    }
                };

                const rawSongs = getData(results[0]);
                const rawAlbums = getData(results[1]);
                const rawPlaylists = getData(results[2]);

                const isPublic = (item) => {
                    const val = item.publiclyVisible;
                    return !(val === false || val === "false");

                };

                const publicSongs = rawSongs.filter(isPublic);
                const publicAlbums = rawAlbums.filter(isPublic);
                const publicPlaylists = rawPlaylists.filter(isPublic);

                setSongs(publicSongs);
                setAlbums(publicAlbums);
                setPlaylists(publicPlaylists);

            } catch (err) {
                console.error("Błąd pobierania danych profilu:", err);
                if (err.response && err.response.status === 404) {
                    setError("Nie znaleziono artysty.");
                }
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            fetchData();
        }
    }, [id]);

    if (loading) {
        return <div className="profile-page page-message">Ładowanie profilu...</div>;
    }

    if (error || !artist) {
        return <div className="profile-page page-message">{error || "Nie znaleziono artysty."}</div>;
    }

    return (
        <div className="profile-page">
            <header className="profile-header">
                <img
                    src={getImageUrl(artist.avatarStorageKeyId ?? artist.avatarId)}
                    alt="Awatar artysty"
                    className="profile-avatar"
                    onError={(e) => { e.target.src = defaultAvatar }}
                />
                <div className="profile-info">
                    <div className="profile-username-wrapper">
                        <h1 className="profile-username">{artist.username}</h1>
                        {artist.emailVerified && (
                            <img src={verifiedBadge} alt="Zweryfikowany" className="verified-badge"
                                 title="Zweryfikowany artysta" />
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

            <section className="profile-content">
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>
                            Utwory
                            <span className="section-count">({songs.length})</span>
                        </h2>
                        <div className="media-grid">
                            {songs && songs.length > 0 ? (
                                songs.slice(0, songsLimit).map(song => (
                                    <MediaCard
                                        key={song.id}
                                        linkTo={`/song/${song.id}`}
                                        imageUrl={getImageUrl(song.coverStorageKeyId)}
                                        title={song.title}
                                        subtitle={`${getYearFromDate(song.createdAt)} • Utwór`}
                                        data={song}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Ten artysta nie udostępnił jeszcze żadnych utworów publicznie.</p>
                            )}
                        </div>
                        <ExpandControls
                            totalCount={songs.length}
                            currentLimit={songsLimit}
                            initialLimit={SONGS_INITIAL_LIMIT}
                            onUpdate={setSongsLimit}
                        />
                    </div>
                )}

                {(activeTab === 'wszystko' || activeTab === 'albumy') && (
                    <div className="content-section">
                        <h2>
                            Albumy
                            <span className="section-count">({albums.length})</span>
                        </h2>
                        <div className="media-grid">
                            {albums && albums.length > 0 ? (
                                albums.slice(0, albumsLimit).map(album => (
                                    <MediaCard
                                        key={album.id}
                                        linkTo={`/album/${album.id}`}
                                        imageUrl={getImageUrl(album.coverStorageKeyId)}
                                        title={album.title}
                                        subtitle={`${getYearFromDate(album.createdAt)} • Album`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Ten artysta nie udostępnił jeszcze żadnych albumów publicznie.</p>
                            )}
                        </div>
                        <ExpandControls
                            totalCount={albums.length}
                            currentLimit={albumsLimit}
                            initialLimit={OTHERS_INITIAL_LIMIT}
                            onUpdate={setAlbumsLimit}
                        />
                    </div>
                )}

                {(activeTab === 'wszystko' || activeTab === 'playlisty') && (
                    <div className="content-section">
                        <h2>
                            Playlisty
                            <span className="section-count">({playlists.length})</span>
                        </h2>
                        <div className="media-grid">
                            {playlists && playlists.length > 0 ? (
                                playlists.slice(0, playlistsLimit).map(playlist => (
                                    <MediaCard
                                        key={playlist.id}
                                        linkTo={`/playlist/${playlist.id}`}
                                        imageUrl={playlist.coverStorageKeyId ? getImageUrl(playlist.coverStorageKeyId) : defaultAvatar}
                                        title={playlist.title}
                                        subtitle={`• Playlista`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Brak publicznych playlist.</p>
                            )}
                        </div>
                        <ExpandControls
                            totalCount={playlists.length}
                            currentLimit={playlistsLimit}
                            initialLimit={OTHERS_INITIAL_LIMIT}
                            onUpdate={setPlaylistsLimit}
                        />
                    </div>
                )}
            </section>
        </div>
    );
}

export default ArtistPage;