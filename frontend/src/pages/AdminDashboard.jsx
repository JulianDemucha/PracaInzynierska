import React, { useState } from 'react';
import { useAuth } from '../context/useAuth.js';
import { searchAll } from '../services/searchService.js';
import { deleteSong } from '../services/songService.js';
import { deleteAlbum } from '../services/albumService.js';
import { deletePlaylist } from '../services/playlistService.js';
import { deleteUserById } from '../services/userService.js';
import { getImageUrl } from '../services/imageService.js';
import defaultAvatar from '../assets/images/default-avatar.png';
import './AdminDashboard.css';

function AdminDashboard() {
    const { currentUser } = useAuth();
    const [query, setQuery] = useState('');
    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');

    if (currentUser?.role !== 'ROLE_ADMIN') {
        return <div className="admin-page">Brak uprawnień.</div>;
    }

    const handleSearch = async (e) => {
        e.preventDefault();
        if (!query) return;
        setLoading(true);
        setMessage('');
        try {
            const data = await searchAll(query);
            setResults(data);
        } catch (error) {
            console.error(error);
            setMessage("Błąd wyszukiwania.");
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (type, id, name) => {
        if (!window.confirm(`Czy na pewno chcesz usunąć ${type}: "${name}"? Ta operacja jest nieodwracalna.`)) return;

        try {
            if (type === 'song') await deleteSong(id);
            if (type === 'album') await deleteAlbum(id);
            if (type === 'playlist') await deletePlaylist(id);
            if (type === 'user') await deleteUserById(id);

            setMessage(`Usunięto ${type}: ${name}`);
            handleSearch({ preventDefault: () => {} });
        } catch (error) {
            console.error(error);
            setMessage("Wystąpił błąd podczas usuwania.");
        }
    };

    const getSafeImgSrc = (item) => {
        if (item.coverUrl) return item.coverUrl;
        if (item.imageUrl) return item.imageUrl;

        const id = item.coverStorageKeyId
            || item.coverId
            || item.avatarStorageKeyId
            || item.storageKeyId;

        return getImageUrl(id) || defaultAvatar;
    };

    const handleImgError = (e) => {
        e.target.src = defaultAvatar;
    };

    return (
        <div className="admin-page">
            <h1>Panel Administratora</h1>
            <div className="admin-search-bar">
                <form onSubmit={handleSearch}>
                    <input
                        type="text"
                        placeholder="Szukaj użytkowników, utworów, albumów..."
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                    />
                    <button type="submit">Szukaj</button>
                </form>
            </div>

            {message && <div className="admin-message">{message}</div>}

            {loading && <div>Ładowanie...</div>}

            {results && (
                <div className="admin-results">
                    <div className="admin-section">
                        <h3>Użytkownicy</h3>
                        {results.users.length === 0 ? <p>Brak</p> : (
                            <ul>
                                {results.users.map(user => (
                                    <li key={user.id} className="admin-item">
                                        <div className="item-info">
                                            <img
                                                src={getSafeImgSrc(user)}
                                                alt="avatar"
                                                className="mini-avatar"
                                                onError={handleImgError}
                                            />
                                            <span>{user.username} (ID: {user.id})</span>
                                        </div>
                                        <button className="delete-btn" onClick={() => handleDelete('user', user.id, user.username)}>USUŃ KONTO</button>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    <div className="admin-section">
                        <h3>Utwory</h3>
                        {results.songs.length === 0 ? <p>Brak</p> : (
                            <ul>
                                {results.songs.map(song => (
                                    <li key={song.id} className="admin-item">
                                        <div className="item-info">
                                            <img
                                                src={getSafeImgSrc(song)}
                                                alt="cover"
                                                className="mini-avatar"
                                                onError={handleImgError}
                                            />
                                            <span>{song.title} - {song.authorUsername}</span>
                                        </div>
                                        <button className="delete-btn" onClick={() => handleDelete('song', song.id, song.title)}>USUŃ</button>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    <div className="admin-section">
                        <h3>Albumy</h3>
                        {results.albums.length === 0 ? <p>Brak</p> : (
                            <ul>
                                {results.albums.map(album => (
                                    <li key={album.id} className="admin-item">
                                        <div className="item-info">
                                            <img
                                                src={getSafeImgSrc(album)}
                                                alt="cover"
                                                className="mini-avatar"
                                                onError={handleImgError}
                                            />
                                            <span>{album.title} - {album.authorUsername}</span>
                                        </div>
                                        <button className="delete-btn" onClick={() => handleDelete('album', album.id, album.title)}>USUŃ</button>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    <div className="admin-section">
                        <h3>Playlisty</h3>
                        {results.playlists.length === 0 ? <p>Brak</p> : (
                            <ul>
                                {results.playlists.map(pl => (
                                    <li key={pl.id} className="admin-item">
                                        <div className="item-info">
                                            <img
                                                src={getSafeImgSrc(pl)}
                                                alt="cover"
                                                className="mini-avatar"
                                                onError={handleImgError}
                                            />
                                            <span>{pl.title} - {pl.creatorUsername}</span>
                                        </div>
                                        <button className="delete-btn" onClick={() => handleDelete('playlist', pl.id, pl.title)}>USUŃ</button>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

export default AdminDashboard;