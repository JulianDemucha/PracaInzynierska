import React, {useState} from "react";
import './PlayerBar.css'
import { Link } from 'react-router-dom';

import './PlayerBar.css';

import playIcon from '../../assets/images/play.png';
import pauseIcon from '../../assets/images/pause.png';
import prevIcon from '../../assets/images/previous.png';
import nextIcon from '../../assets/images/next.png';
import shuffleIcon from '../../assets/images/shuffle.png';
import repeatIcon from '../../assets/images/repeat.png';
import soundIcon from '../../assets/images/sound.png';
import noSoundIcon from '../../assets/images/noSound.png';
import maxSoundIcon from '../../assets/images/maxSound.png';
import queueIcon from '../../assets/images/kolejka.png';

import albumArtPlaceholder from '../../assets/images/logo.png';

function PlayerBar() {
    const [isPlaying, setIsPlaying] = useState(false);

    const [volume, setVolume] = useState(50);
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
    const [previousVolume, setPreviousVolume] = useState(80);
    const toggleMute = () => {
        if (Number(volume) > 0) {
            // Jesteśmy na głośności, więc wyciszamy
            setPreviousVolume(volume); // Zapamiętaj aktualną głośność
            setVolume(0);              // Ustaw głośność na 0
        } else {
            // Jesteśmy wyciszeni, więc przywracamy
            // Jeśli ktoś wyciszył, a potem ręcznie ustawił suwak na 0,
            // przywróćmy domyślną głośność (np. 50), a nie 0.
            setVolume(previousVolume > 0 ? previousVolume : 50);
        }
    };

    return (
        <footer className="player-bar">

            {/* ============================================= */}
            {/* ========= SEKCJA LEWA (Informacje) ========== */}
            {/* ============================================= */}
            <div className="player-section-left">
                <img src={albumArtPlaceholder} alt="Okładka albumu" className="player-album-art" />
                <div className="player-song-details">
                    <span className="player-song-title">Nazwa Utworu</span>
                    <Link to="/artist/1" className="player-artist-name">Nazwa Artysty</Link>
                </div>
                <button className="control-button like-button">
                    ♡ {/* Placeholder dla ikony "Lubię to" */}
                </button>
            </div>

            {/* ============================================= */}
            {/* ========= SEKCJA ŚRODKOWA (Kontrolki) ======= */}
            {/* ============================================= */}
            <div className="player-section-middle">
                {/* 1. Górne przyciski (Shuffle, Prev, Play, Next, Repeat) */}
                <div className="player-controls">
                    <button className="control-button shuffle">
                        <img src={shuffleIcon} alt="Losowe" />
                    </button>
                    <button className="control-button prev">
                        <img src={prevIcon} alt="Poprzedni" />
                    </button>

                    {/* Przycisk Play/Pause ze zmianą ikony */}
                    <button
                        className="control-button play"
                        onClick={() => setIsPlaying(!isPlaying)}
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
                    <button className="control-button repeat">
                        <img src={repeatIcon} alt="Pętla" />
                    </button>
                </div>

                {/* 2. Pasek postępu */}
                <div className="progress-bar-container">
                    <span className="time-current">0:42</span>
                    <input type="range" className="progress-bar" min="0" max="100" defaultValue="25" />
                    <span className="time-duration">2:58</span>
                </div>
            </div>

            {/* ============================================= */}
            {/* ========= SEKCJA PRAWA (Głośność) =========== */}
            {/* ============================================= */}
            <div className="player-section-right">
                <button className="control-button queue-button">
                    <img src={queueIcon} alt="Kolejka" />
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

        </footer>
    );
}

export default PlayerBar;