import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import './ArtistPage.css';
import defaultAvatar from '../assets/images/default-avatar.png';
import verifiedBadge from '../assets/images/verified.png';
import MediaCard from '../components/cards/MediaCard.jsx';
import api from '../context/axiosClient.js';
import {getImageUrl} from '../services/imageService.js';

/** nie wiem jak to mialo dzialac skoro tu byl mock wszystkiego zlepionego razem i request lecial na aktualnego usera po /api/users/me*/

// ew zmienic na pelna date, ale niby na spotify tez jest sam rok
const getYearFromDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).getFullYear();
};


function ArtistPage() {
    const {id} = useParams()
    const [artist, setArtist] = useState(null);
    const [songs, setSongs] = useState(null);
    const [albums, setAlbums] = useState(null);
    const [playlists, setPlaylists] = useState([]); //narazie pusta lista poki playlist nie mamy

    const [activeTab, setActiveTab] = useState('wszystko');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                // user
                const userPromise = api.get(`/users/${id}`);

                // piosenki
                const songsPromise = api.get(`/songs/user/${id}`);

                // albumy
                const albumsPromise = api.get(`/albums/user/${id}`);

                // todo dodac fetchowanie playlist jak juz beda

                const [userRes, songsRes, albumsRes] =
                    await Promise.all([userPromise, songsPromise, albumsPromise]);

                setArtist(userRes.data);
                setSongs(songsRes.data);
                setAlbums(albumsRes.data);
            } catch (err) {
                console.error("Błąd pobierania danych profilu:", err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            fetchData();
        }
    }, [id]);

    if(loading){
        return <div className="profile-page">Ładowanie profilu...</div>;
    }

    if (error || !artist) {
        return <div className="profile-page">Nie znaleziono artysty.</div>; // lub wystąpił błąd idk
    }

    return (
        <div className="profile-page">

            <header className="profile-header">
                <img
                    src={defaultAvatar} //todo jak na backendzie bedzie obsluga image dla usera to oblsluzyc tu
                    alt="Awatar artysty"
                    className="profile-avatar"
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
                        {artist.bio || "brak bio"}
                    </p>
                    {/** jak skoro mamy to createdat to mozna cos takiego dodac*/}
                    {/*<p className="tu sobie ogarnij ja nie znam sie na geografii nie bede ci w cssie grzebal">*/}
                    {/*    Dołączył: {getYearFromDate(artist.createdAt)}*/}
                    {/*</p>*/}
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

            {/* ===== 3. ZAWARTOŚĆ (ZMIENIONA NA SIATKĘ KART) ===== */}
            <section className="profile-content">

                {/* --- Pokaż "utworey" --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Utwory</h2>
                        <div className="media-grid">
                            {songs.length > 0 ? (
                                songs.map(song => (
                                    <MediaCard
                                        key={song.id}
                                        linkTo={`/song/${song.id}`}
                                        imageUrl={getImageUrl(song.coverStorageKeyId) || defaultAvatar}
                                        title={song.title}
                                        subtitle={`${getYearFromDate(song.createdAt)} • ${song.genres?.[0] || 'Utwór'}`}
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
                            {albums.length > 0 ? (
                                albums.map(album => (
                                    <MediaCard
                                        key={album.id}
                                        linkTo={`/album/${album.id}`}
                                        imageUrl={defaultAvatar} // todo jak na backendzie bedzie obsluga image dla albumu to obsluzyc tu
                                        title={album.title}
                                        subtitle={`${getYearFromDate(album.createdAt)} • Album`} />
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
                            {playlists.length > 0 ? (
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
                                <p className="empty-tab-message">Brak playlist.</p>
                            )}
                        </div>
                    </div>
                )}
            </section>
        </div>
    );
}

export default ArtistPage;