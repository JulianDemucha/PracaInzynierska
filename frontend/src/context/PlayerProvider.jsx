import React, { useState, useRef, useEffect } from 'react';
import { PlayerContext } from './PlayerContext.js';

export function PlayerProvider({ children }) {
    const audioRef = useRef(null);

    const [currentSong, setCurrentSong] = useState(null);
    const [isPlaying, setIsPlaying] = useState(false);
    const [queue, setQueue] = useState([]);
    const [history, setHistory] = useState([]);

    const [favorites, setFavorites] = useState({});
    const [ratings, setRatings] = useState({});

    const [volume, setVolume] = useState(0.5);
    const [previousVolume, setPreviousVolume] = useState(0.8);

    const [currentTime, setCurrentTime] = useState(0);
    const [duration, setDuration] = useState(0);

    const [isShuffleOn, setIsShuffleOn] = useState(false);
    const [isRepeatOn, setIsRepeatOn] = useState(false);
    const [isRepeatOneOn, setIsRepeatOneOn] = useState(false);

    useEffect(() => {
        const audio = audioRef.current;
        if (!audio) return;

        const onTimeUpdate = () => setCurrentTime(audio.currentTime || 0);

        const onLoadedMetadata = () => {
            setDuration(isFinite(audio.duration) ? audio.duration : 0);
            setCurrentTime(audio.currentTime || 0);
        };

        const onEnded = () => {
            if (isRepeatOneOn) {
                audio.currentTime = 0;
                audio.play();
                return;
            }
            playNext();
        };

        audio.addEventListener('timeupdate', onTimeUpdate);
        audio.addEventListener('loadedmetadata', onLoadedMetadata);
        audio.addEventListener('ended', onEnded);

        return () => {
            audio.removeEventListener('timeupdate', onTimeUpdate);
            audio.removeEventListener('loadedmetadata', onLoadedMetadata);
            audio.removeEventListener('ended', onEnded);
        };
    }, [isRepeatOneOn]);

    useEffect(() => {
        const audio = audioRef.current;
        if (!audio) return;
        audio.volume = Number(volume);
    }, [volume]);

    useEffect(() => {
        const audio = audioRef.current;
        if (!audio) return;

        if (!currentSong) {
            audio.pause();
            audio.src = '';
            setIsPlaying(false);
            setCurrentTime(0);
            setDuration(0);
            return;
        }

        const src = `/api/songs/stream/${currentSong.id}`;

        if (!audio.src || !audio.src.endsWith(String(currentSong.id))) {
            audio.src = src;
            audio.load();
        }

        const tryPlay = async () => {
            try {
                await audio.play();
                setIsPlaying(true);
            } catch {
                setIsPlaying(false);
            }
        };
        tryPlay();
    }, [currentSong]);

    const playSong = (song, songList = null) => {
        if (!song) return;

        if (songList) {
            const idx = songList.findIndex(s => s.id === song.id);
            if (idx >= 0) {
                setQueue(songList.slice(idx + 1));
            } else {
                setQueue(songList);
            }
        }

        const audio = audioRef.current;
        if (currentSong && song.id === currentSong.id) {
            if (audio) {
                audio.play()
                    .then(() => setIsPlaying(true))
                    .catch(() => setIsPlaying(false));
            } else {
                setIsPlaying(true);
            }
            return;
        }

        if (currentSong) {
            setHistory(h => [...h, currentSong]);
        }

        setCurrentSong(song);
    };

    const pause = () => {
        const audio = audioRef.current;
        if (audio) {
            audio.pause();
        }
        setIsPlaying(false);
    };

    const playNext = () => {
        if (isShuffleOn && queue.length > 0) {
            const idx = Math.floor(Math.random() * queue.length);
            const next = queue[idx];

            setQueue(q => {
                const copy = [...q];
                copy.splice(idx, 1);
                return copy;
            });

            setHistory(h => [...h, currentSong].filter(Boolean));
            setCurrentSong(next);
            return;
        }

        if (queue.length > 0) {
            const [next, ...rest] = queue;
            setQueue(rest);
            setHistory(h => [...h, currentSong].filter(Boolean));
            setCurrentSong(next);
            return;
        }

        if (isRepeatOn && history.length > 0) {
            const first = history[0];
            setQueue([]);
            setHistory([]);
            setCurrentSong(first);
            return;
        }

        setIsPlaying(false);
    };

    const playPrev = () => {
        if (history.length === 0) {
            const audio = audioRef.current;
            if (audio) {
                audio.currentTime = 0;
            }
            return;
        }
        const prev = history[history.length - 1];
        setHistory(h => h.slice(0, -1));
        setQueue(q => [currentSong, ...q].filter(Boolean));
        setCurrentSong(prev);
    };

    const seekTo = (seconds) => {
        const audio = audioRef.current;
        if (!audio) return;
        audio.currentTime = Math.max(0, Math.min(seconds, audio.duration || 0));
        setCurrentTime(audio.currentTime);
    };

    const toggleFavorite = (songId) => {
        setFavorites(prev => ({ ...prev, [songId]: !prev[songId] }));
    };

    const addToQueue = (song) => {
        setQueue(prevQueue => [...prevQueue, song]);
    };

    const rateSong = (songId, voteType) => {
        setRatings(prev => {
            const currentRating = prev[songId];
            if (currentRating === voteType) {
                const newState = { ...prev };
                delete newState[songId];
                return newState;
            }
            return { ...prev, [songId]: voteType };
        });
    };

    const toggleShuffle = () => setIsShuffleOn(s => !s);

    const toggleRepeat = () => {
        if (!isRepeatOn && !isRepeatOneOn) {
            setIsRepeatOn(true);
            setIsRepeatOneOn(false);
        } else if (isRepeatOn && !isRepeatOneOn) {
            setIsRepeatOneOn(true);
            setIsRepeatOn(false);
        } else {
            setIsRepeatOn(false);
            setIsRepeatOneOn(false);
        }
    };

    const toggleMute = () => {
        if (volume > 0) {
            setPreviousVolume(volume);
            setVolume(0);
        } else {
            setVolume(previousVolume || 0.5);
        }
    };

    const setVolumePercent = (percent) => {
        const v = Math.max(0, Math.min(100, Number(percent)));
        setVolume(v / 100);
        setPreviousVolume(v / 100);
    };

    const value = {
        currentSong,
        isPlaying,
        queue,
        playSong,
        pause,
        addToQueue,
        favorites,
        toggleFavorite,
        ratings,
        rateSong,
        playNext,
        playPrev,
        seekTo,
        setVolumePercent,
        toggleMute,
        currentTime,
        duration,
        uiVolumePercent: Math.round(volume * 100),
        isShuffleOn,
        isRepeatOn,
        isRepeatOneOn,
        toggleShuffle,
        toggleRepeat,
    };

    return (
        <PlayerContext.Provider value={value}>
            <audio
                ref={audioRef}
                crossOrigin="use-credentials"
                style={{ display: 'none' }}
            />
            {children}
        </PlayerContext.Provider>
    );
}