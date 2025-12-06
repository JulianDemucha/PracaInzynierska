import React, { useState, useEffect, useMemo } from 'react';
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import { usePlayer } from '../context/PlayerContext.js';
import { useAuth } from '../context/useAuth.js';
import { getImageUrl } from '../services/imageService.js';
import {
    getAlbumById,
    getSongsByAlbumId,
    deleteAlbum,
    removeSongFromAlbum
} from '../services/albumService.js';
import {
    getPlaylistById,
    getPlaylistSongs,
    deletePlaylist,
    removeSongFromPlaylist,
    getUserPlaylists,
    changeSongPosition
} from '../services/playlistService.js';
import {
    likeSong,
    dislikeSong,
    removeLike,
    removeDislike,
    addSongToFavorites,
    removeSongFromFavorites
} from '../services/songService.js';

import CreateAlbumModal from '../components/album/CreateAlbumModal.jsx';
import AddToPlaylistModal from '../components/playlist/AddToPlaylistModal.jsx';
import RemoveFromPlaylistModal from '../components/playlist/RemoveFromPlaylistModal.jsx';
import ContextMenu from '../components/common/ContextMenu.jsx';
import EditPlaylistModal from '../components/playlist/EditPlaylistModal.jsx';

import binIcon from '../assets/images/bin.png';
import defaultCover from '../assets/images/default-avatar.png';
import playIcon from '../assets/images/play.png';
import pauseIcon from '../assets/images/pause.png';
import heartIconOff from '../assets/images/favorites.png';
import heartIconOn from '../assets/images/favoritesOn.png';
import likeIcon from '../assets/images/like.png';
import likeIconOn from '../assets/images/likeOn.png';
import dislikeIcon from '../assets/images/disLike.png';
import dislikeIconOn from '../assets/images/disLikeOn.png';
import plusIcon from '../assets/images/plus.png';
import editIcon from '../assets/images/edit.png';

import './CollectionPage.css';

function formatTime(seconds) {
    if (!seconds || isNaN(seconds)) return "0:00";
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m}:${s.toString().padStart(2, '0')}`;
}

function CollectionPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const { currentUser } = useAuth();

    const isPlaylist = location.pathname.includes('/playlist');

    const [collection, setCollection] = useState(null);
    const [songs, setSongs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [songsPlaylistCount, setSongsPlaylistCount] = useState({});

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [isAddSongModalOpen, setIsAddSongModalOpen] = useState(false);
    const [isAddToPlaylistModalOpen, setIsAddToPlaylistModalOpen] = useState(false);
    const [songToAddToPlaylist, setSongToAddToPlaylist] = useState(null);
    const [isRemovePlaylistModalOpen, setIsRemovePlaylistModalOpen] = useState(false);
    const [songToRemoveFromPlaylist, setSongToRemoveFromPlaylist] = useState(null);

    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isReorderModalOpen, setIsReorderModalOpen] = useState(false);
    const [songToReorder, setSongToReorder] = useState(null);
    const [newPositionValue, setNewPositionValue] = useState("");

    const {
        currentSong, isPlaying, playSong, pause, addToQueue,
        favorites, toggleFavorite, ratings, rateSong
    } = usePlayer();

    const fetchData = async () => {
        try {
            if (!collection) setLoading(true);
            setError(null);

            let fetchedCollection = null;
            let fetchedSongs = [];

            if (isPlaylist) {
                const playlistData = await getPlaylistById(id);
                fetchedCollection = {
                    id: playlistData.id,
                    title: playlistData.title,
                    description: playlistData.description || "",
                    authorId: playlistData.creatorId,
                    authorName: playlistData.creatorUsername,
                    coverStorageKeyId: playlistData.coverStorageKeyId,
                    createdAt: playlistData.createdAt,
                    publiclyVisible: playlistData.publiclyVisible,
                    type: 'PLAYLIST'
                };

                try {
                    const playlistSongsData = await getPlaylistSongs(id);
                    fetchedSongs = playlistSongsData.map(item => {
                        const s = item.song || item;
                        return {
                            id: s.id,
                            title: s.title,
                            duration: s.duration || 0,
                            authorId: s.authorId,
                            authorUsername: s.authorUsername,
                            coverStorageKeyId: s.coverStorageKeyId,
                            artist: { id: s.authorId, name: s.authorUsername || "Nieznany" },
                            coverArtUrl: getImageUrl(s.coverStorageKeyId),
                            genres: s.genres || [],
                            position: item.positionInPlaylist,
                            publiclyVisible: s.publiclyVisible,
                            albumId: s.albumId
                        };
                    });
                } catch (songErr) {
                    console.error("Błąd pobierania piosenek playlisty:", songErr);
                    fetchedSongs = [];
                }

            } else {
                const [albumData, songsData] = await Promise.all([
                    getAlbumById(id),
                    getSongsByAlbumId(id)
                ]);

                fetchedCollection = {
                    ...albumData,
                    authorName: albumData.authorName || albumData.authorUsername,
                    type: 'ALBUM'
                };

                fetchedSongs = songsData.map(s => ({
                    ...s,
                    artist: { id: s.authorId, name: s.authorUsername || "Nieznany" },
                    coverArtUrl: getImageUrl(albumData.coverStorageKeyId),
                    duration: s.duration || 0
                }));
            }

            setCollection(fetchedCollection);
            setSongs(fetchedSongs);

        } catch (err) {
            console.error("Błąd pobierania kolekcji:", err);
            if (err.response && err.response.status === 403) {
                setError("Brak dostępu (Prywatna).");
            } else {
                setError("Nie udało się załadować zawartości.");
            }
        } finally {
            setLoading(false);
        }
    };

    const fetchUserPlaylistData = async () => {
        if (!currentUser) return;
        try {
            const userPlaylists = await getUserPlaylists(currentUser.id);
            if (!userPlaylists || userPlaylists.length === 0) return;

            const counts = {};
            await Promise.all(userPlaylists.map(async (pl) => {
                try {
                    const songsInPl = await getPlaylistSongs(pl.id);
                    songsInPl.forEach(item => {
                        const songId = item.song?.id || item.id || item.songId;
                        if (songId) {
                            counts[songId] = (counts[songId] || 0) + 1;
                        }
                    });
                } catch (e) {
                    console.warn(`Błąd dla playlisty ${pl.id}`, e);
                }
            }));
            setSongsPlaylistCount(counts);
        } catch (e) {
            console.error("Błąd analizy playlist użytkownika:", e);
        }
    };

    useEffect(() => {
        setCollection(null);
        setSongs([]);
        fetchData();
    }, [id, isPlaylist]);

    useEffect(() => {
        fetchUserPlaylistData();
    }, [currentUser]);

    const albumGenres = useMemo(() => {
        if (!songs || songs.length === 0) return [];
        const allGenres = songs.flatMap(song => song.genres || []);
        return [...new Set(allGenres)];
    }, [songs]);

    const handleDeleteClick = () => setIsDeleteModalOpen(true);

    const confirmDelete = async () => {
        setIsDeleting(true);
        try {
            if (isPlaylist) {
                await deletePlaylist(collection.id);
            } else {
                await deleteAlbum(collection.id);
            }
            navigate('/profile');
        } catch (err) {
            console.error("Błąd usuwania:", err);
            alert("Nie udało się usunąć.");
        } finally {
            setIsDeleting(false);
            setIsDeleteModalOpen(false);
        }
    };

    const handleRemoveSong = async (songId, songTitle) => {
        const msg = isPlaylist
            ? `Czy usunąć utwór "${songTitle}" z tej playlisty?`
            : `Czy usunąć utwór "${songTitle}" z albumu?`;

        if (!window.confirm(msg)) return;

        try {
            if (isPlaylist) {
                await removeSongFromPlaylist(collection.id, songId);
            } else {
                await removeSongFromAlbum(collection.id, songId);
            }
            fetchData();
            fetchUserPlaylistData();
        } catch (err) {
            console.error("Błąd usuwania piosenki:", err);
            alert("Nie udało się usunąć piosenki.");
        }
    };

    const openReorderModal = (song, currentIndex) => {
        setSongToReorder(song);
        setNewPositionValue(currentIndex + 1);
        setIsReorderModalOpen(true);
    };

    const handleConfirmReorder = async (e) => {
        e.preventDefault();
        if (!songToReorder) return;
        const pos = parseInt(newPositionValue, 10);
        if (isNaN(pos) || pos < 1 || pos > songs.length) {
            alert(`Podaj poprawny numer od 1 do ${songs.length}`);
            return;
        }
        try {
            const apiPosition = pos - 1;
            await changeSongPosition(collection.id, songToReorder.id, apiPosition);
            setIsReorderModalOpen(false);
            setSongToReorder(null);
            await fetchData();
        } catch (err) {
            console.error("Błąd zmiany pozycji:", err);
            alert("Wystąpił błąd podczas zmiany kolejności.");
        }
    };

    const isOwner = currentUser && collection && (currentUser.id == collection.authorId);

    if (loading) return <div className="collection-page page-message">Ładowanie...</div>;
    if (error) return <div className="collection-page page-message">{error} <br /><Link to="/">Wróć</Link></div>;
    if (!collection) return <div className="collection-page page-message">Nie znaleziono.</div>;

    const handlePlayCollection = () => {
        if (songs.length === 0) return;
        const firstSong = songs[0];
        if (currentSong?.id === firstSong.id) {
            if (isPlaying) pause(); else playSong(firstSong);
        } else {
            playSong(firstSong, songs);
        }
    };

    const handlePlayTrack = (song) => {
        if (currentSong?.id === song.id) {
            if (isPlaying) pause(); else playSong(song);
        } else {
            playSong(song, songs);
        }
    };

    const handleFavoriteSongClick = async (songId) => {
        const isCurrentlyFavorite = !!favorites[songId];
        toggleFavorite(songId);

        try {
            if (isCurrentlyFavorite) {
                await removeSongFromFavorites(songId);
            } else {
                await addSongToFavorites(songId);
            }
        } catch (error) {
            console.error("Błąd aktualizacji ulubionych:", error);
            toggleFavorite(songId);
        }
    };

    const handleRatingSongClick = async (songId, type) => {
        rateSong(songId, type);

        try {
            if (type === 'like') {
                if (ratings[songId] === 'like') {
                    await removeLike(songId);
                } else {
                    await likeSong(songId);
                }
            } else if (type === 'dislike') {
                if (ratings[songId] === 'dislike') {
                    await removeDislike(songId);
                } else {
                    await dislikeSong(songId);
                }
            }
        } catch (error) {
            console.error("Błąd aktualizacji oceny:", error);
        }
    };

    const headerMenuOptions = [
        { label: "Dodaj wszystkie do kolejki", onClick: () => songs.forEach(s => addToQueue(s)) },
    ];
    const isAnySongFromCollectionPlaying = songs.some(s => s.id === currentSong?.id);
    const showPauseOnHeader = isAnySongFromCollectionPlaying && isPlaying;

    return (
        <div className="collection-page">
            <header className="song-header">
                <img
                    src={getImageUrl(collection.coverStorageKeyId)}
                    alt={collection.title}
                    className="song-cover-art"
                    onError={(e) => { e.target.src = defaultCover }}
                />
                <div className="song-details">
                    <span className="song-type">{isPlaylist ? "PLAYLISTA" : "ALBUM"}</span>
                    <h1>{collection.title}</h1>
                    <div className="song-meta">
                        <Link to={`/artist/${collection.authorId}`} className="song-artist">
                            {collection.authorName || "Nieznany"}
                        </Link>
                        <span>•</span>
                        <span>{new Date(collection.createdAt).getFullYear()}</span>
                        <span>•</span>
                        <span className="song-duration">{songs.length} utworów</span>
                    </div>
                    {!isPlaylist && (
                        <div className="genre-tags">
                            {albumGenres.map(genre => (
                                <Link key={genre} to={`/genre/${genre}`} className="genre-pill">{genre}</Link>
                            ))}
                        </div>
                    )}
                    {!collection.publiclyVisible && (
                        <div className="private-badge-container">
                            <span className="private-badge">Prywatny</span>
                        </div>
                    )}
                </div>
            </header>

            <section className="song-controls">
                <div className="collection-controls-row">
                    <button className="song-play-button" onClick={handlePlayCollection}>
                        <img src={showPauseOnHeader ? pauseIcon : playIcon} alt="Play/Pause" />
                    </button>

                    {isOwner && !isPlaylist && (
                        <button
                            className="add-song-circle-btn"
                            onClick={() => setIsAddSongModalOpen(true)}
                            title="Dodaj utwór"
                        >
                            <img src={plusIcon} alt="Dodaj do albumu" />
                        </button>
                    )}

                    {isOwner && (
                        <div className="owner-controls">
                            {isPlaylist && (
                                <button
                                    className="song-control-button icon-btn edit-playlist-btn"
                                    onClick={() => setIsEditModalOpen(true)}
                                    title="Edytuj playlistę"
                                >
                                    <img src={editIcon} alt="Edytuj" className="icon-small" />
                                </button>
                            )}
                            <button className="delete-song-button icon-btn" onClick={handleDeleteClick} title="Usuń">
                                <img src={binIcon} alt="Usuń" />
                            </button>
                        </div>
                    )}
                    <ContextMenu options={headerMenuOptions} />
                </div>
            </section>

            <section className="song-list-container">
                <div className="song-list-header">
                    <span className="song-header-track">#</span>
                    <span className="song-header-title">TYTUŁ</span>
                    <span className="song-header-actions"></span>
                    <span className="song-header-duration">CZAS</span>
                    <span className="col-spacer"></span>
                </div>

                <ul className="song-list">
                    {songs.map((song, index) => {
                        const isActive = currentSong?.id === song.id;
                        const isPlayingNow = isActive && isPlaying;
                        const isLiked = !!favorites[song.id];
                        const songRating = ratings[song.id];
                        const playlistCount = songsPlaylistCount[song.id] || 0;
                        const showGlobalRemove = playlistCount > 1;

                        const songMenuOptions = [
                            { label: isLiked ? "Usuń z polubionych" : "Dodaj do polubionych", onClick: () => handleFavoriteSongClick(song.id) },
                            { label: "Dodaj do kolejki", onClick: () => addToQueue(song) },
                            { label: "Przejdź do artysty", onClick: () => navigate(`/artist/${song.artist.id}`) },
                            { label: "Przejdź do utworu", onClick: () => navigate(`/song/${song.id}`) },
                            { label: "Dodaj do playlisty", onClick: () => { setSongToAddToPlaylist(song); setIsAddToPlaylistModalOpen(true); } }
                        ];

                        if (showGlobalRemove) {
                            songMenuOptions.push({
                                label: "Usuń z playlisty",
                                onClick: () => { setSongToRemoveFromPlaylist(song); setIsRemovePlaylistModalOpen(true); }
                            });
                        }

                        if (isOwner) {
                            songMenuOptions.push({
                                label: isPlaylist ? "Usuń z tej playlisty" : "Usuń z albumu",
                                onClick: () => handleRemoveSong(song.id, song.title),
                            });

                            if (isPlaylist) {
                                songMenuOptions.push({
                                    label: "Zmień pozycję",
                                    onClick: () => openReorderModal(song, index)
                                });
                            }
                        }

                        return (
                            <li key={`${song.id}-${index}`} className={`song-list-item ${isActive ? 'active' : ''}`} onDoubleClick={() => handlePlayTrack(song)}>
                                <span className="song-track-number">
                                    {isPlayingNow ? <img src={pauseIcon} alt="Pauza" onClick={() => pause()} /> :
                                        <div className="number-container">
                                            <span className="track-number">{index + 1}</span>
                                            <img src={playIcon} alt="Odtwórz" className="play-icon-hover" onClick={() => handlePlayTrack(song)} />
                                        </div>}
                                </span>
                                <div className="song-item-details">
                                    <span className={`song-item-title ${isActive ? 'highlight' : ''}`}>{song.title}</span>
                                    <Link to={`/artist/${song.artist.id}`} className="song-item-artist" onClick={(e) => e.stopPropagation()}>
                                        {song.artist.name}
                                    </Link>
                                </div>
                                <div className="song-item-actions">
                                    <button
                                        className={`action-btn ${isLiked ? 'active' : ''}`}
                                        onClick={() => handleFavoriteSongClick(song.id)}
                                    >
                                        <img src={isLiked ? heartIconOn : heartIconOff} alt="Like" />
                                    </button>
                                    <button
                                        className={`action-btn ${songRating === 'like' ? 'active' : ''}`}
                                        onClick={() => handleRatingSongClick(song.id, 'like')}
                                    >
                                        <img src={songRating === 'like' ? likeIconOn : likeIcon} alt="Up" />
                                    </button>
                                    <button
                                        className={`action-btn ${songRating === 'dislike' ? 'active' : ''}`}
                                        onClick={() => handleRatingSongClick(song.id, 'dislike')}
                                    >
                                        <img src={songRating === 'dislike' ? dislikeIconOn : dislikeIcon} alt="Down" />
                                    </button>
                                </div>
                                <span className="song-item-duration">{formatTime(song.duration)}</span>
                                <div className="song-context-menu-wrapper">
                                    <ContextMenu options={songMenuOptions} />
                                </div>
                            </li>
                        );
                    })}
                </ul>
            </section>

            {isDeleteModalOpen && (
                <div className="delete-modal-backdrop" onClick={() => setIsDeleteModalOpen(false)}>
                    <div className="delete-modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>Usunąć {isPlaylist ? 'playlistę' : 'album'} "{collection?.title}"?</h3>
                        <p className="warning-text">
                            {isPlaylist ? "Playlista zostanie usunięta." : "Piosenki zostaną trwale usunięte!"}
                        </p>
                        <div className="delete-modal-actions">
                            <button className="cancel-btn" onClick={() => setIsDeleteModalOpen(false)} disabled={isDeleting}>Anuluj</button>
                            <button className="confirm-delete-btn" onClick={confirmDelete} disabled={isDeleting}>{isDeleting ? "Usuwanie..." : "Usuń"}</button>
                        </div>
                    </div>
                </div>
            )}
            {!isPlaylist && <CreateAlbumModal isOpen={isAddSongModalOpen} onClose={() => setIsAddSongModalOpen(false)} existingAlbumId={collection?.id} onAlbumUpdate={fetchData} />}
            <AddToPlaylistModal isOpen={isAddToPlaylistModalOpen} onClose={() => setIsAddToPlaylistModalOpen(false)} songToAdd={songToAddToPlaylist} />
            <RemoveFromPlaylistModal isOpen={isRemovePlaylistModalOpen} onClose={() => setIsRemovePlaylistModalOpen(false)} songToRemove={songToRemoveFromPlaylist} />

            {isReorderModalOpen && (
                <div className="delete-modal-backdrop" onClick={() => setIsReorderModalOpen(false)}>
                    <div className="delete-modal-content small-modal" onClick={(e) => e.stopPropagation()}>
                        <h3>Zmień pozycję utworu</h3>
                        <p className="modal-subtitle">"{songToReorder?.title}"</p>

                        <form onSubmit={handleConfirmReorder}>
                            <div className="modal-form-group">
                                <label className="input-label">Nowa pozycja (1 - {songs.length}):</label>
                                <input type="number" min="1" max={songs.length} value={newPositionValue} onChange={(e) => setNewPositionValue(e.target.value)} autoFocus className="modal-input" />
                            </div>

                            <div className="delete-modal-actions">
                                <button type="button" className="cancel-btn" onClick={() => setIsReorderModalOpen(false)}>Anuluj</button>
                                <button type="submit" className="confirm-delete-btn save-btn">Zapisz</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
            <EditPlaylistModal
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)}
                playlistToEdit={collection}
                onPlaylistUpdated={fetchData}
                playlistSongs={songs}
            />
        </div>
    );
}

export default CollectionPage;