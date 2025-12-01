import React, { useState, useEffect } from 'react';
import { useAuth } from "../context/useAuth.js";
import { Link, useNavigate } from 'react-router-dom'; // Dodano useNavigate
import './ProfilePage.css';
import defaultAvatar from '../assets/images/default-avatar.png';

// Komponenty
import EditProfileModal from '../components/profile/EditProfileModal.jsx';
import AddSongModal from '../components/song/AddSongModal.jsx';
import CreateAlbumModal from '../components/album/CreateAlbumModal.jsx';
import MediaCard from '../components/cards/MediaCard.jsx';

// Serwisy
import { getUserSongs } from '../services/songService.js';
import { getUserAlbums } from '../services/albumService.js';
import { getImageUrl } from "../services/imageService.js";
import { deleteUserAccount } from "../services/userService.js"; // <--- NOWY IMPORT (stwórz ten plik/funkcję)

// --- DANE TESTOWE ---
const mockOtherContent = {
    playlists: [
        { id: 1, title: "Moja playlista nr 1", year: 2022, coverArtUrl: "https://placehold.co/200x200/1DB954/white?text=Playlista+1", type: "Playlista" },
        { id: 2, title: "Moja playlista nr 2", year: 2021, coverArtUrl: "https://placehold.co/200x200/E73C7E/white?text=Playlista+2", type: "Playlista" },
    ],
    comments: [
        { id: 1, text: "Mój komentarz 1..." }
    ]
};

function ProfilePage() {
    const { currentUser, logout, loading } = useAuth();
    const navigate = useNavigate();

    const [activeTab, setActiveTab] = useState('wszystko');
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isAddSongModalOpen, setIsAddSongModalOpen] = useState(false);
    const [isCreateAlbumModalOpen, setIsCreateAlbumModalOpen] = useState(false);

    // NOWY STAN: Modal usuwania konta
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    const [userSongs, setUserSongs] = useState([]);
    const [userAlbums, setUserAlbums] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            if (currentUser?.id) {
                try {
                    const songsData = await getUserSongs(currentUser.id);
                    setUserSongs(songsData);
                    const albumsData = await getUserAlbums(currentUser.id);
                    setUserAlbums(albumsData);
                } catch (error) {
                    console.error("Błąd pobierania danych profilu:", error);
                }
            }
        };
        fetchData();
    }, [currentUser]);

    const formattedJoinDate = currentUser?.createdAt
        ? new Date(currentUser.createdAt).toLocaleDateString('pl-PL')
        : 'Nieznana data';

    // --- NOWA FUNKCJA USUWANIA KONTA ---
    const handleDeleteAccount = async () => {
        setIsDeleting(true);
        try {
            await deleteUserAccount();
            // Po udanym usunięciu wyloguj i przekieruj
            logout();
            navigate('/');
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
            {/* ===== NAGŁÓWEK ===== */}
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
                        <p className="profile-bio-text" style={{ color: '#b3b3b3', margin: '0.5rem 0', fontSize: '0.95rem', maxWidth: '600px' }}>
                            {currentUser.bio}
                        </p>
                    )}
                    <p className="profile-joined-date" style={{ color: '#aaa', fontSize: '0.85rem', marginTop: '0.5rem' }}>
                        Dołączył: {formattedJoinDate}
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
                    <button className="add-song-button" onClick={() => setIsAddSongModalOpen(true)}>Dodaj utwór</button>
                    <button className="add-album-button" onClick={() => setIsCreateAlbumModalOpen(true)}>Dodaj album</button>
                    <button className="edit-profile-button" onClick={() => setIsEditModalOpen(true)}>Edytuj profil</button>

                    {/* PRZYCISK WYLOGUJ */}
                    <Link to="/" className="logout-button" onClick={logout}>Wyloguj</Link>

                    {/* NOWY PRZYCISK: USUŃ KONTO */}
                    <button className="delete-account-button" onClick={() => setIsDeleteModalOpen(true)}>
                        Usuń konto
                    </button>
                </div>
            </nav>

            {/* ===== ZAWARTOŚĆ ===== */}
            <section className="profile-content">
                {/* ... (Bez zmian w sekcjach contentu) ... */}

                {/* --- Pokaż "Własne utwory" --- */}
                {(activeTab === 'wszystko' || activeTab === 'wlasne') && (
                    <div className="content-section">
                        <h2>Własne utwory</h2>
                        <div className="media-grid">
                            {userSongs.length > 0 ? (
                                userSongs.map(song => (
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
                    </div>
                )}

                {/* --- Sekcja Albumy --- */}
                {(activeTab === 'wszystko' || activeTab === 'albumy') && (
                    <div className="content-section">
                        <h2>Albumy</h2>
                        <div className="media-grid">
                            {userAlbums.length > 0 ? (
                                userAlbums.map(album => (
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
            <EditProfileModal isOpen={isEditModalOpen} onClose={() => setIsEditModalOpen(false)} />
            <AddSongModal isOpen={isAddSongModalOpen} onClose={() => setIsAddSongModalOpen(false)} />
            <CreateAlbumModal isOpen={isCreateAlbumModalOpen} onClose={() => setIsCreateAlbumModalOpen(false)} />

            {/* --- NOWY MODAL: POTWIERDZENIE USUNIĘCIA KONTA --- */}
            {isDeleteModalOpen && (
                <div className="delete-modal-backdrop" onClick={() => setIsDeleteModalOpen(false)}>
                    <div className="delete-modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Usunąć konto?</h3>
                        <p style={{color: '#b3b3b3', marginBottom: '1.5rem'}}>
                            Czy na pewno chcesz usunąć swoje konto? <br/>
                            <span style={{color: '#ff4444', fontWeight: 'bold'}}>
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