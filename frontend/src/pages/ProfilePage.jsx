import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from "../context/useAuth.js";
import { Link } from 'react-router-dom';
import defaultAvatar from '../assets/images/default-avatar.png';

import EditProfileModal from '../components/profile/EditProfileModal.jsx';
import AddSongModal from '../components/song/AddSongModal.jsx';
import CreateAlbumModal from '../components/album/CreateAlbumModal.jsx';
import MediaCard from '../components/cards/MediaCard.jsx';

import { getUserSongs } from '../services/songService.js';
import { getUserAlbums } from '../services/albumService.js';
import { getUserPlaylists } from '../services/playlistService.js';
import { getImageUrl } from "../services/imageService.js";
import { deleteUserAccount } from "../services/userService.js";

import './ProfilePage.css';

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

function ProfilePage() {
    const { currentUser, logout, loading } = useAuth();

    const [activeTab, setActiveTab] = useState('wszystko');
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isAddSongModalOpen, setIsAddSongModalOpen] = useState(false);
    const [isCreateAlbumModalOpen, setIsCreateAlbumModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    const [userSongs, setUserSongs] = useState([]);
    const [userAlbums, setUserAlbums] = useState([]);
    const [userPlaylists, setUserPlaylists] = useState([]);

    const [songsLimit, setSongsLimit] = useState(SONGS_INITIAL_LIMIT);
    const [albumsLimit, setAlbumsLimit] = useState(OTHERS_INITIAL_LIMIT);
    const [playlistsLimit, setPlaylistsLimit] = useState(OTHERS_INITIAL_LIMIT);

    const fetchSongs = useCallback(async () => {
        if (!currentUser?.id) return;
        try {
            const songsData = await getUserSongs(currentUser.id);
            setUserSongs(songsData);
        } catch (error) {
            console.error("Błąd pobierania utworów:", error);
        }
    }, [currentUser?.id]);

    const fetchAlbums = useCallback(async () => {
        if (!currentUser?.id) return;
        try {
            const albumsData = await getUserAlbums(currentUser.id);
            setUserAlbums(albumsData);
        } catch (error) {
            console.error("Błąd pobierania albumów:", error);
        }
    }, [currentUser?.id]);

    const fetchPlaylists = useCallback(async () => {
        if (!currentUser?.id) return;
        try {
            const playlistsData = await getUserPlaylists(currentUser.id);
            setUserPlaylists(playlistsData);
        } catch (error) {
            console.error("Błąd pobierania playlist:", error);
        }
    }, [currentUser?.id]);

    useEffect(() => {
        if (currentUser?.id) {
            fetchSongs();
            fetchAlbums();
            fetchPlaylists();
        }
    }, [currentUser, fetchSongs, fetchAlbums, fetchPlaylists]);

    const formattedJoinDate = currentUser?.createdAt
        ? new Date(currentUser.createdAt).toLocaleDateString('pl-PL')
        : 'Nieznana data';

    const handleDeleteAccount = async () => {
        setIsDeleting(true);
        try {
            await deleteUserAccount();
            logout();
            window.location.href = '/';
        } catch (error) {
            console.error("Błąd usuwania konta:", error);
            alert("Nie udało się usunąć konta. Spróbuj ponownie.");
            setIsDeleting(false);
            setIsDeleteModalOpen(false);
        }
    };

    if (loading && !currentUser) {
        return <div className="profile-page"><p>Ładowanie profilu…</p></div>;
    }

    return (
        <div className="profile-page">
            <header className="profile-header">
                <img
                    src={getImageUrl(currentUser.avatarStorageKeyId) || defaultAvatar}
                    alt="Awatar użytkownika"
                    className="profile-avatar"
                />
                <div className="profile-info">
                    <div className="profile-username-wrapper">
                        <h1 className="profile-username">{currentUser?.username || 'Nazwa Użytkownika'}</h1>
                    </div>
                    {currentUser?.bio && (
                        <p className="profile-bio-text">
                            {currentUser.bio}
                        </p>
                    )}
                    <p className="profile-joined-date">
                        Dołączył: {formattedJoinDate}
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
                <div className="profile-nav-actions">
                    <button className="add-song-button" onClick={() => setIsAddSongModalOpen(true)}>Dodaj utwór</button>
                    <button className="add-album-button" onClick={() => setIsCreateAlbumModalOpen(true)}>Dodaj album</button>
                    <button className="edit-profile-button" onClick={() => setIsEditModalOpen(true)}>Edytuj profil</button>

                    <Link to="/" className="logout-button" onClick={logout}>Wyloguj</Link>

                    <button className="delete-account-button" onClick={() => setIsDeleteModalOpen(true)}>
                        Usuń konto
                    </button>
                </div>
            </nav>

            <section className="profile-content">
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>
                            Własne utwory
                            <span className="section-count">({userSongs.length})</span>
                        </h2>
                        <div className="media-grid">
                            {userSongs.length > 0 ? (
                                userSongs.slice(0, songsLimit).map(song => (
                                    <MediaCard
                                        key={song.id}
                                        linkTo={`/song/${song.id}`}
                                        imageUrl={getImageUrl(song.coverStorageKeyId)}
                                        title={song.title}
                                        subtitle={`${new Date(song.createdAt).getFullYear()} • Utwór`}
                                        data={song}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Nie dodałeś jeszcze żadnych utworów.</p>
                            )}
                        </div>
                        <ExpandControls
                            totalCount={userSongs.length}
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
                            <span className="section-count">({userAlbums.length})</span>
                        </h2>
                        <div className="media-grid">
                            {userAlbums.length > 0 ? (
                                userAlbums.slice(0, albumsLimit).map(album => (
                                    <MediaCard
                                        key={album.id}
                                        linkTo={`/album/${album.id}`}
                                        imageUrl={album.coverStorageKeyId ? getImageUrl(album.coverStorageKeyId) : defaultAvatar}
                                        title={album.title}
                                        subtitle={`${new Date(album.createdAt).getFullYear()} • Album`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Nie dodałeś jeszcze żadnych albumów.</p>
                            )}
                        </div>
                        <ExpandControls
                            totalCount={userAlbums.length}
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
                            <span className="section-count">({userPlaylists.length})</span>
                        </h2>
                        <div className="media-grid">
                            {userPlaylists.length > 0 ? (
                                userPlaylists.slice(0, playlistsLimit).map(playlist => (
                                    <MediaCard
                                        key={playlist.id}
                                        linkTo={`/playlist/${playlist.id}`}
                                        imageUrl={playlist.coverStorageKeyId ? getImageUrl(playlist.coverStorageKeyId) : defaultAvatar}
                                        title={playlist.title || playlist.name}
                                        subtitle={`${playlist.songsCount || 0} utworów • Playlista`}
                                    />
                                ))
                            ) : (
                                <p className="empty-tab-message">Nie stworzyłeś jeszcze żadnych playlist.</p>
                            )}
                        </div>
                        <ExpandControls
                            totalCount={userPlaylists.length}
                            currentLimit={playlistsLimit}
                            initialLimit={OTHERS_INITIAL_LIMIT}
                            onUpdate={setPlaylistsLimit}
                        />
                    </div>
                )}
            </section>

            <EditProfileModal
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)} />
            <AddSongModal
                isOpen={isAddSongModalOpen}
                onClose={() => setIsAddSongModalOpen(false)}
                onSongAdded={fetchSongs}
            />

            <CreateAlbumModal
                isOpen={isCreateAlbumModalOpen}
                onClose={() => setIsCreateAlbumModalOpen(false)}
                onAlbumUpdate={fetchAlbums}
                onSongsUpdate={fetchSongs}
            />

            {isDeleteModalOpen && (
                <div className="delete-modal-backdrop" onClick={() => setIsDeleteModalOpen(false)}>
                    <div className="delete-modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Usunąć konto?</h3>
                        <p className="delete-modal-description">
                            Czy na pewno chcesz usunąć swoje konto? <br/>
                            <span className="delete-modal-warning">
                                Ta operacja jest nieodwracalna. Stracisz wszystkie swoje utwory, albumy i playlisty.
                            </span>
                        </p>
                        <div className="delete-modal-actions">
                            <button
                                className="cancel-btn"
                                onClick={() => setIsDeleteModalOpen(false)}
                                disabled={isDeleting}
                            >
                                Anuluj
                            </button>
                            <button
                                className="confirm-delete-btn"
                                onClick={handleDeleteAccount}
                                disabled={isDeleting}
                            >
                                {isDeleting ? "Usuwanie..." : "Tak, usuń konto"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default ProfilePage;