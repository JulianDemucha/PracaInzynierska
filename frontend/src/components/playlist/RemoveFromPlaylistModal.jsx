import React, { useState, useEffect } from 'react';
import { getUserPlaylists, getPlaylistSongs, removeSongFromPlaylist } from '../../services/playlistService.js';
import { useAuth } from '../../context/useAuth.js';
import './RemoveFromPlaylistModal.css';

function RemoveFromPlaylistModal({ isOpen, onClose, songToRemove }) {
    const { currentUser } = useAuth();

    const [playlists, setPlaylists] = useState([]); // Playlisty zawierające ten utwór
    const [loading, setLoading] = useState(false);

    // Reset i pobieranie danych przy otwarciu
    useEffect(() => {
        if (isOpen && currentUser && songToRemove) {
            findPlaylistsWithSong();
        } else {
            setPlaylists([]);
        }
    }, [isOpen, currentUser, songToRemove]);

    const findPlaylistsWithSong = async () => {
        setLoading(true);
        try {
            // 1. Pobierz wszystkie playlisty użytkownika
            const allPlaylists = await getUserPlaylists(currentUser.id);

            if (!allPlaylists || allPlaylists.length === 0) {
                setPlaylists([]);
                setLoading(false);
                return;
            }

            // 2. Sprawdź każdą playlistę, czy zawiera ten utwór
            // Uwaga: To generuje dużo zapytań (N+1). Docelowo backend powinien mieć endpoint /api/songs/{id}/playlists
            const playlistsWithSong = [];

            // Używamy Promise.all dla szybkości
            await Promise.all(allPlaylists.map(async (pl) => {
                try {
                    const songsInPlaylist = await getPlaylistSongs(pl.id);
                    // Sprawdzamy czy songToRemove.id znajduje się w tej playliście
                    // Backend zwraca PlaylistSongViewDto, który ma pole 'song' lub 'songId' lub płaską strukturę.
                    // Zakładam, że w liście jest obiekt, który ma id piosenki.
                    // Jeśli PlaylistSongViewDto ma pole 'id' (wpisu) i 'songId' (piosenki), szukamy po songId.

                    const exists = songsInPlaylist.some(item => item.id === songToRemove.id || item.songId === songToRemove.id);

                    if (exists) {
                        playlistsWithSong.push(pl);
                    }
                } catch (err) {
                    console.error(`Błąd sprawdzania playlisty ${pl.id}`, err);
                }
            }));

            setPlaylists(playlistsWithSong);
        } catch (error) {
            console.error("Błąd szukania playlist:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleRemove = async (playlistId, playlistName) => {
        if (!window.confirm(`Czy na pewno usunąć utwór z playlisty "${playlistName}"?`)) return;

        try {
            await removeSongFromPlaylist(playlistId, songToRemove.id);
            alert(`Usunięto z playlisty "${playlistName}".`);
            // Odśwież listę w modalu (usuń tę playlistę z widoku)
            setPlaylists(prev => prev.filter(pl => pl.id !== playlistId));

            // Jeśli to była ostatnia playlista, zamknij modal
            if (playlists.length <= 1) {
                onClose();
            }
        } catch (error) {
            console.error("Błąd usuwania:", error);
            alert("Nie udało się usunąć utworu.");
        }
    };

    if (!isOpen) return null;

    return (
        <div className="remove-modal-backdrop" onClick={onClose}>
            <div className="remove-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>

                <h2>Usuń z playlisty</h2>
                {songToRemove && (
                    <p className="song-preview-text">
                        Wybrany utwór: <strong>{songToRemove.title}</strong>
                    </p>
                )}

                <div className="playlists-list-container">
                    {loading ? (
                        <p style={{textAlign: 'center', color: '#888'}}>Sprawdzanie playlist...</p>
                    ) : (
                        playlists.length > 0 ? (
                            <div className="playlists-scroll-area">
                                {playlists.map(pl => (
                                    <div key={pl.id} className="playlist-remove-row">
                                        <div className="playlist-info">
                                            <span className="playlist-name">{pl.title || pl.name}</span>
                                            <span className="playlist-count">
                                                {pl.songsCount} utworów
                                            </span>
                                        </div>
                                        <button
                                            className="remove-btn-small"
                                            onClick={() => handleRemove(pl.id, pl.title || pl.name)}
                                        >
                                            Usuń
                                        </button>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="empty-state">
                                <p>Ten utwór nie znajduje się na żadnej z Twoich playlist.</p>
                                <button className="close-modal-btn" onClick={onClose}>Zamknij</button>
                            </div>
                        )
                    )}
                </div>
            </div>
        </div>
    );
}

export default RemoveFromPlaylistModal;