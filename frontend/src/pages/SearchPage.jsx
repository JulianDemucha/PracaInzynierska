import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { searchAll } from '../services/searchService.js';
import { getImageUrl } from '../services/imageService.js';
import MediaCard from '../components/cards/MediaCard.jsx';
import defaultAvatar from '../assets/images/default-avatar.png';
import { usePlayer } from '../context/PlayerContext.js';

import './SearchPage.css';

function SearchPage() {
    const [searchParams] = useSearchParams();
    const query = searchParams.get('q');

    const [results, setResults] = useState({ songs: [], albums: [], playlists: [], users: [] });
    const [loading, setLoading] = useState(false);

    const { playSong } = usePlayer();

    useEffect(() => {
        if (query) {
            setLoading(true);
            searchAll(query)
                .then(data => setResults(data))
                .catch(err => console.error("Błąd wyszukiwania:", err))
                .finally(() => setLoading(false));
        }
    }, [query]);

    if (!query) return <div className="search-page-msg">Wpisz frazę, aby wyszukać.</div>;
    if (loading) return <div className="search-page-msg">Wyszukiwanie...</div>;

    const hasResults = results.songs.length > 0 || results.albums.length > 0 || results.playlists.length > 0 || results.users.length > 0;

    if (!hasResults) return <div className="search-page-msg">Brak wyników dla "{query}".</div>;

    return (
        <div className="search-page custom-scrollbar">
            <h1 className="search-header">Wyniki wyszukiwania dla "{query}"</h1>

            {results.songs.length > 0 && (
                <section className="search-section">
                    <h2>Utwory</h2>
                    <div className="search-songs-list">
                        {results.songs.map((song) => {
                            const songForPlayer = {
                                ...song,
                                coverUrl: getImageUrl(song.coverStorageKeyId) || defaultAvatar,
                                author: song.authorUsername || song.author
                            };

                            return (
                                <div
                                    key={song.id}
                                    className="search-song-row"
                                    // 2. Tutaj przekazujemy ten "naprawiony" obiekt
                                    onDoubleClick={() => playSong(songForPlayer)}
                                >
                                    <div className="search-song-img-wrapper">
                                        <Link to={song.albumId ? `/album/${song.albumId}` : `/song/${song.id}`}>
                                            <img
                                                src={getImageUrl(song.coverStorageKeyId)}
                                                onError={(e) => e.target.src = defaultAvatar}
                                                alt={song.title}
                                            />
                                        </Link>
                                    </div>

                                    <div className="search-song-info">
                                        <Link to={`/song/${song.id}`} className="search-song-title">
                                            {song.title}
                                        </Link>

                                        <Link to={`/artist/${song.authorId}`} className="search-song-artist">
                                            {song.authorUsername}
                                        </Link>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </section>
            )}

            {results.albums.length > 0 && (
                <section className="search-section">
                    <h2>Albumy</h2>
                    <div className="media-grid">
                        {results.albums.map(album => (
                            <MediaCard
                                key={album.id}
                                linkTo={`/album/${album.id}`}
                                imageUrl={getImageUrl(album.coverStorageKeyId)}
                                title={album.title}
                                subtitle={album.authorUsername}
                            />
                        ))}
                    </div>
                </section>
            )}

            {results.playlists.length > 0 && (
                <section className="search-section">
                    <h2>Playlisty</h2>
                    <div className="media-grid">
                        {results.playlists.map(playlist => (
                            <MediaCard
                                key={playlist.id}
                                linkTo={`/playlist/${playlist.id}`}
                                imageUrl={getImageUrl(playlist.coverStorageKeyId)}
                                title={playlist.title}
                                subtitle={`Autor: ${playlist.creatorUsername}`}
                            />
                        ))}
                    </div>
                </section>
            )}

            {results.users.length > 0 && (
                <section className="search-section">
                    <h2>Użytkownicy</h2>
                    <div className="media-grid">
                        {results.users.map(user => (
                            <Link key={user.id} to={`/artist/${user.id}`} className="search-user-card">
                                <img
                                    src={getImageUrl(user.avatarStorageKeyId) || defaultAvatar}
                                    alt={user.username}
                                    className="search-user-avatar"
                                />
                                <span className="search-user-name">{user.username}</span>
                            </Link>
                        ))}
                    </div>
                </section>
            )}
        </div>
    );
}

export default SearchPage;