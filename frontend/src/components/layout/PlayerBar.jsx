import React, { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';

import './PlayerBar.css';
import '../../index.css';
import '../common/ContextMenu.css';
import ContextMenu from '../common/ContextMenu.jsx';
import { usePlayer } from '../../context/PlayerContext';


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


function PlayerBar() {
    const { currentSong, isPlaying, playSong, pause } = usePlayer();

    const [isShuffleOn, setIsShuffleOn] = useState(false);
    const [isRepeatOn, setIsRepeatOn] = useState(false);
    const [isFavoriteOn, setIsFavoriteOn] = useState(false);
    const [isQueueVisible, setIsQueueVisible] = useState(false);
    const queuePopupRef = useRef(null);
    const [previousVolume, setPreviousVolume] = useState(80);
    const [volume, setVolume] = useState(50);

    // const [isPlaying, setIsPlaying] = useState(false);

    const getVolumeIcon = () => {
        const currentVolume = Number(volume);
        if (currentVolume === 0) {
            return <img src={noSoundIcon} alt="Mute" />;
        }
        if (currentVolume === 100) {
            return <img src={maxSoundIcon} alt="Max" />;
        }
        return <img src={soundIcon} alt="Mute" />;
    };
    const toggleMute = () => {
        if (Number(volume) > 0) {
            setPreviousVolume(volume);
            setVolume(0);
        } else {
            setVolume(previousVolume > 0 ? previousVolume : 50);
        }
    };

    const mockQueue = [
        { id: 1, title: "Nazwa Utworu 1", artist: "Artysta 1" },
        { id: 2, title: "Kolejna piosenka", artist: "Inny Artysta" },
        { id: 3, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 4, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 5, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 6, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 7, title: "Kolejna piosenka", artist: "Inny Artysta" },
        { id: 8, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 9, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 10, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 11, title: "Nazwa Utworu 1", artist: "Artysta 1" },
        { id: 12, title: "Kolejna piosenka", artist: "Inny Artysta" },
        { id: 13, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 14, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 15, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 16, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 17, title: "Kolejna piosenka", artist: "Inny Artysta" },
        { id: 18, title: "Trzeci utwór w kolejce", artist: "Zespół" },
        { id: 19, title: "Trzeci utwór w kolejce", artist: "Zespół" },
    ];

    const handleRemoveFromQueue = (songId) => {
        console.log("Usunięto piosenkę o ID:", songId);
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (queuePopupRef.current && !queuePopupRef.current.contains(event.target)) {
                setIsQueueVisible(false);
            }
        };
        if (isQueueVisible) {
            document.addEventListener("mousedown", handleClickOutside);
        }
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [isQueueVisible]);

    const playerMenuOptions = [
        {
            label: "Dodaj do polubionych",
            onClick: () => console.log("Dodano do polubionych")
        },
        {
            label: "Dodaj do kolejki",
            onClick: () => console.log("Dodano do kolejki")
        },
        {
            label: "Przejdź do artysty",
            onClick: () => console.log("Przechodzę do artysty")
        }
    ];

    // --- 4. ZAKTUALIZOWANA FUNKCJA PLAY/PAUSE ---
    const handlePlayPauseClick = () => {
        if (!currentSong) return;

        if (isPlaying) {
            pause();
        } else {
            playSong(currentSong);
        }
    };

    const songTitle = currentSong?.title || "Wybierz utwór";
    const artistName = currentSong?.artist?.name || "SoundSpace";
    const artistLink = `/artist/${currentSong?.artist?.id || ''}`;
    const coverArt = currentSong?.coverArtUrl || albumArtPlaceholder;

    return (
        <footer className="player-bar">

            {/* ========= SEKCJA LEWA (Informacje) ========== */}
            <div className="player-section-left">
                <img src={coverArt} alt="Okładka albumu" className="player-album-art" />
                <div className="player-song-details">
                    <span className="player-song-title">{songTitle}</span>
                    <Link to={artistLink} className="player-artist-name">{artistName}</Link>
                </div>
                <button
                    className={`control-button favorite-button ${isFavoriteOn ? 'active' : ''}`}
                    onClick={() => {setIsFavoriteOn(!isFavoriteOn)}}
                >
                    {isFavoriteOn ? (
                        <img src={favoriteIconOn} alt="Polubione" />
                    ) : (
                        <img src={favoriteIcon} alt="Niepolubione" />
                    )}
                </button>
            </div>

            {/* ========= SEKCJA ŚRODKOWA (Kontrolki) ======= */}
            <div className="player-section-middle">
                <div className="player-controls">

                    <button className="control-button prev">
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

                    <button className="control-button next">
                        <img src={nextIcon} alt="Następny" />
                    </button>

                    {/* Pasek postępu (na razie statyczny) */}
                    <div className="progress-bar-container">
                        <span className="time-current">1:30</span>
                        <input type="range" className="progress-bar" min="0" max="100" defaultValue="50" />
                        <span className="time-duration">3:00</span>
                    </div>

                    <button
                        className={`control-button shuffle ${isShuffleOn ? 'active' : ''}`}
                        onClick={() => setIsShuffleOn(!isShuffleOn)}
                    >
                        {isShuffleOn ? (
                            <img src={shuffleIconOn} alt="Losowe (Włączone)" />
                        ) : (
                            <img src={shuffleIcon} alt="Losowe (Wyłączone)" />
                        )}
                    </button>

                    <button
                        className={`control-button repeat ${isRepeatOn ? 'active' : ''}`}
                        onClick={() => setIsRepeatOn(!isRepeatOn)}
                    >
                        {isRepeatOn ? (
                            <img src={repeatIconOn} alt="Pętla (Włączona)" />
                        ) : (
                            <img src={repeatIcon} alt="Pętla (Wyłączona)" />
                        )}
                    </button>
                </div>
            </div>

            {/* ========= SEKCJA PRAWA (Głośność) =========== */}
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
                    value={volume}
                    onChange={(e) => {
                        setVolume(e.target.value)
                        setPreviousVolume(e.target.value);
                    }}
                />
            </div>
            {isQueueVisible && (
                <div className="queue-popup custom-scrollbar" ref={queuePopupRef}>
                    <h3 className="queue-title">Kolejka odtwarzania</h3>
                    <ul className="queue-list">
                        {mockQueue.map((song) => (
                            <li key={song.id} className="queue-item">
                                <div className="queue-song-details">
                                    <span className="queue-song-title">{song.title}</span>
                                    <span className="queue-artist-name">{song.artist}</span>
                                </div>
                                <button
                                    className="queue-remove-button"
                                    onClick={() => handleRemoveFromQueue(song.id)}
                                >
                                    <img src={deleteIcon} alt="Delete" />
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </footer>
    );
}

export default PlayerBar;