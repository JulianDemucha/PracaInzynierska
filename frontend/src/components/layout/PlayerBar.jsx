import React, { useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { usePlayer } from '../../context/PlayerContext.js';
import { addSongToFavorites, removeSongFromFavorites } from '../../services/songService.js'; // IMPORT SERWISÓW
import ContextMenu from '../common/ContextMenu.jsx';

import './PlayerBar.css';
import '../../index.css';
import '../common/ContextMenu.css';

import playIcon from '../../assets/images/play.png';
import pauseIcon from '../../assets/images/pause.png';
import prevIcon from '../../assets/images/previous.png';
import nextIcon from '../../assets/images/next.png';
import shuffleIcon from '../../assets/images/shuffle.png';
import repeatIcon from '../../assets/images/repeat.png';
import shuffleIconOn from '../../assets/images/shuffleOn.png';
import repeatIconOn from '../../assets/images/repeatOn.png';
import soundIcon from '../../assets/images/sound.png';
import noSoundIcon from '../../assets/images/noSound.png';
import maxSoundIcon from '../../assets/images/maxSound.png';
import queueIcon from '../../assets/images/queue.png';
import queueIconOn from '../../assets/images/queueVisible.png';
import favoriteIcon from '../../assets/images/favorites.png';
import favoriteIconOn from '../../assets/images/favoritesOn.png';
import albumArtPlaceholder from '../../assets/images/logo.png';
import deleteIcon from '../../assets/images/bin.png';

function formatTime(seconds) {
    if (!seconds || !isFinite(seconds)) return '0:00';
    const s = Math.floor(seconds % 60).toString().padStart(2, '0');
    const m = Math.floor(seconds / 60);
    return `${m}:${s}`;
}

function PlayerBar() {
    const navigate = useNavigate();
    const {
        currentSong,
        isPlaying,
        playNext,
        playPrev,
        playSong,
        pause,
        queue,
        removeFromQueue,
        favorites,
        toggleFavorite,
        currentTime,
        duration,
        seekTo,
        setVolumePercent,
        uiVolumePercent,
        toggleShuffle,
        isShuffleOn,
        toggleRepeat,
        isRepeatOn,
        isRepeatOneOn,
        toggleMute
    } = usePlayer();

    const [isQueueVisible, setIsQueueVisible] = React.useState(false);
    const queuePopupRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (queuePopupRef.current && !queuePopupRef.current.contains(event.target)) {
                if (!event.target.closest('.queue-button')) {
                    setIsQueueVisible(false);
                }
            }
        };
        if (isQueueVisible) {
            document.addEventListener("mousedown", handleClickOutside);
        }
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [isQueueVisible]);

    const getVolumeIcon = () => {
        const currentVolume = uiVolumePercent;
        if (currentVolume === 0) {
            return <img src={noSoundIcon} alt="Mute" />;
        }
        if (currentVolume === 100) {
            return <img src={maxSoundIcon} alt="Max" />;
        }
        return <img src={soundIcon} alt="Sound" />;
    };

    const handleRemoveFromQueue = (index, e) => {
        e.stopPropagation();
        if (removeFromQueue) {
            removeFromQueue(index);
        }
    };

    const artistName = (currentSong?.authorUsername) || "SoundSpace";
    const artistId = currentSong?.authorId;
    const artistLink = artistId ? `/artist/${artistId}` : '#';
    const songTitle = currentSong?.title || "Wybierz utwór";
    const coverArt = currentSong?.coverArtUrl || albumArtPlaceholder;

    const isCurrentSongFavorite = currentSong && favorites ? !!favorites[currentSong.id] : false;

    const handleFavoriteClick = async () => {
        if (!currentSong) return;

        const songId = currentSong.id;
        const wasFavorite = isCurrentSongFavorite;

        toggleFavorite(songId);

        try {
            if (wasFavorite) {
                await removeSongFromFavorites(songId);
            } else {
                await addSongToFavorites(songId);
            }
        } catch (error) {
            console.error("Błąd aktualizacji ulubionych:", error);
            toggleFavorite(songId);
        }
    };

    const playerMenuOptions = [
        {
            label: isCurrentSongFavorite ? "Usuń z polubionych" : "Dodaj do polubionych",
            onClick: () => handleFavoriteClick()
        },
        {
            label: "Dodaj do kolejki",
            onClick: () => console.log("Dodano do kolejki")
        },
        {
            label: "Przejdź do artysty",
            onClick: () => {
                if (artistId) {
                    navigate(artistLink);
                } else {
                    console.log("Brak ID artysty lub artysta nieznany");
                }
            }
        }
    ];

    const handlePlayPauseClick = () => {
        if (!currentSong) return;
        if (isPlaying) {
            pause();
        } else {
            playSong(currentSong);
        }
    };

    const handleSeekChange = (e) => {
        const pct = Number(e.target.value);
        if (!duration || !isFinite(duration)) return;
        const seconds = (pct / 100) * duration;
        seekTo(seconds);
    };

    const handleVolumeChange = (e) => {
        setVolumePercent(e.target.value);
    };

    const progressPercent = duration ? (currentTime / duration) * 100 : 0;
    const volumePercent = uiVolumePercent;

    return (
        <footer className="player-bar">
            <div className="player-section-left">
                <img src={coverArt} alt="Okładka albumu" className="player-album-art" />
                <div className="player-song-details">
                    <span className="player-song-title">{songTitle}</span>
                    <Link to={artistLink} className="player-artist-name">
                        {artistName}
                    </Link>
                </div>
                <button
                    className={`control-button favorite-button ${isCurrentSongFavorite ? 'active' : ''}`}
                    onClick={handleFavoriteClick}
                    disabled={!currentSong}
                >
                    {isCurrentSongFavorite ? (
                        <img src={favoriteIconOn} alt="Polubione" />
                    ) : (
                        <img src={favoriteIcon} alt="Niepolubione" />
                    )}
                </button>
            </div>

            <div className="player-section-middle">
                <div className="player-controls">
                    <button className="control-button prev" onClick={playPrev}>
                        <img src={prevIcon} alt="Poprzedni" />
                    </button>

                    <button
                        className="control-button play"
                        onClick={handlePlayPauseClick}
                        disabled={!currentSong}
                    >
                        {isPlaying ? (
                            <img src={pauseIcon} alt="Pauza" />
                        ) : (
                            <img src={playIcon} alt="Odtwórz" />
                        )}
                    </button>

                    <button className="control-button next" onClick={playNext}>
                        <img src={nextIcon} alt="Następny" />
                    </button>

                    <div className="progress-bar-container">
                        <span className="time-current">{formatTime(currentTime)}</span>
                        <input
                            type="range"
                            className="progress-bar"
                            min="0"
                            max="100"
                            step="0.1"
                            value={progressPercent}
                            onChange={handleSeekChange}
                            style={{
                                background: `linear-gradient(to right, #8A2BE2 ${progressPercent}%, #4d4d4d ${progressPercent}%)`
                            }}
                        />
                        <span className="time-duration">{formatTime(duration)}</span>
                    </div>

                    <button
                        className={`control-button shuffle ${isShuffleOn ? 'active' : ''}`}
                        onClick={() => toggleShuffle()}
                    >
                        {isShuffleOn ? (
                            <img src={shuffleIconOn} alt="Losowe (Włączone)" />
                        ) : (
                            <img src={shuffleIcon} alt="Losowe (Wyłączone)" />
                        )}
                    </button>

                    <button
                        className={`control-button repeat ${isRepeatOn || isRepeatOneOn ? 'active' : ''}`}
                        onClick={() => toggleRepeat()}
                    >
                        {isRepeatOneOn ? (
                            <img src={repeatIconOn} alt="Powtarzanie jednego" />
                        ) : (
                            <img src={repeatIcon} alt="Powtarzanie" />
                        )}
                    </button>
                </div>
            </div>

            <div className="player-section-right">
                <ContextMenu options={playerMenuOptions} />
                <button
                    className={`control-button queue-button ${isQueueVisible ? 'active' : ''}`}
                    onClick={() => {setIsQueueVisible(!isQueueVisible)}}
                >
                    {isQueueVisible ? (
                        <img src={queueIconOn} alt="Kolejka (Otwarta)" />
                    ) : (
                        <img src={queueIcon} alt="Kolejka (Zamknięta)" />
                    )}
                </button>
                <div className="volume-icon" onClick={() => toggleMute()}>
                    {getVolumeIcon()}
                </div>

                <input
                    type="range"
                    className="volume-slider"
                    min="0"
                    max="100"
                    value={uiVolumePercent}
                    onChange={handleVolumeChange}
                    style={{
                        background: `linear-gradient(to right, #8A2BE2 ${volumePercent}%, #4d4d4d ${volumePercent}%)`
                    }}
                />
            </div>

            {isQueueVisible && (
                <div className="queue-popup custom-scrollbar" ref={queuePopupRef}>
                    <h3 className="queue-title">Kolejka odtwarzania</h3>
                    <ul className="queue-list">
                        {queue.length > 0 ? (
                            queue.map((song, index) => (
                                <li key={`${song.id}-${index}`} className="queue-item">
                                    <div className="queue-song-details">
                                        <span className="queue-song-title">{song.title}</span>
                                        <span className="queue-artist-name">
                                            {song.artist?.name || (typeof song.artist === 'string' ? song.artist : "Nieznany")}
                                        </span>
                                    </div>
                                    <button
                                        className="queue-remove-button"
                                        onClick={(e) => handleRemoveFromQueue(index, e)}
                                    >
                                        <img src={deleteIcon} alt="Delete" />
                                    </button>
                                </li>
                            ))
                        ) : (
                            <li className="queue-item" style={{justifyContent: 'center'}}>Pusta kolejka</li>
                        )}
                    </ul>
                </div>
            )}
        </footer>
    );
}

export default PlayerBar;