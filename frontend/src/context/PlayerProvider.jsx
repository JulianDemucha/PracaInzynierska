import React, { useState, useRef, useEffect } from 'react';
import { PlayerContext } from './PlayerContext.js';
import { registerView } from '../services/songService.js';

export function PlayerProvider({ children }) {
    const audioRef = useRef(null);

    const accumulatedTimeRef = useRef(0);
    const lastTimeRef = useRef(0);
    const viewRegisteredRef = useRef(false);

    const [currentSong, setCurrentSong] = useState(null);
    const [isPlaying, setIsPlaying] = useState(false);
    const [queue, setQueue] = useState([]);
    const [history, setHistory] = useState([]);

    const [favorites, setFavorites] = useState({});
    const [ratings, setRatings] = useState({});

    const [viewUpdateTrigger, setViewUpdateTrigger] = useState(0);

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

        const onTimeUpdate = () => {
            const now = audio.currentTime || 0;
            setCurrentTime(now);

            if (now < 0.5 && lastTimeRef.current > 1.0) {
                accumulatedTimeRef.current = 0;
                viewRegisteredRef.current = false;
            }

            if (currentSong && isFinite(audio.duration) && audio.duration > 0) {
                const timeDiff = now - lastTimeRef.current;

                if (timeDiff > 0 && timeDiff < 1.5) {
                    if (!viewRegisteredRef.current) {
                        accumulatedTimeRef.current += timeDiff;
                    }
                }

                lastTimeRef.current = now;

                const totalDuration = audio.duration;
                let threshold = totalDuration >= 60 ? 30 : totalDuration * 0.40;

                if (!viewRegisteredRef.current && accumulatedTimeRef.current >= threshold) {
                    registerView(currentSong.id)
                        .then(() => setViewUpdateTrigger(prev => prev + 1))
                        .catch(err => console.error(err));

                    viewRegisteredRef.current = true;
                }
            }
        };

        const onLoadedMetadata = () => {
            setDuration(isFinite(audio.duration) ? audio.duration : 0);
            setCurrentTime(audio.currentTime || 0);
        };

        const onEnded = () => {
            if (isRepeatOneOn) {
                audio.currentTime = 0;
                accumulatedTimeRef.current = 0;
                viewRegisteredRef.current = false;
                lastTimeRef.current = 0;
                audio.play();
                return;
            }
            playNext();
        };

        const onPlay = () => { lastTimeRef.current = audio.currentTime; };

        audio.addEventListener('timeupdate', onTimeUpdate);
        audio.addEventListener('loadedmetadata', onLoadedMetadata);
        audio.addEventListener('ended', onEnded);
        audio.addEventListener('play', onPlay);

        return () => {
            audio.removeEventListener('timeupdate', onTimeUpdate);
            audio.removeEventListener('loadedmetadata', onLoadedMetadata);
            audio.removeEventListener('ended', onEnded);
            audio.removeEventListener('play', onPlay);
        };
    }, [currentSong, isRepeatOneOn]);

    useEffect(() => {
        if (audioRef.current) audioRef.current.volume = Number(volume);
    }, [volume]);

    useEffect(() => {
        const audio = audioRef.current;
        if (!audio) return;

        accumulatedTimeRef.current = 0;
        viewRegisteredRef.current = false;
        lastTimeRef.current = 0;

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

        audio.play().then(() => setIsPlaying(true)).catch(() => setIsPlaying(false));
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

        if (currentSong && song.id === currentSong.id) {
            if (audioRef.current) {
                audioRef.current.play()
                    .then(() => setIsPlaying(true))
                    .catch(() => setIsPlaying(false));
            } else {
                setIsPlaying(true);
            }
            return;
        }

        if (currentSong) setHistory(h => [...h, currentSong]);
        setCurrentSong(song);
    };

    const pause = () => {
        if (audioRef.current) audioRef.current.pause();
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
        if (audioRef.current && audioRef.current.currentTime > 3) {
            audioRef.current.currentTime = 0;
            return;
        }

        if (history.length > 0) {
            const prev = history[history.length - 1];
            setHistory(h => h.slice(0, -1));
            if (currentSong) setQueue(q => [currentSong, ...q]);
            setCurrentSong(prev);
        } else {
            if (audioRef.current) audioRef.current.currentTime = 0;
        }
    };

    const seekTo = (seconds) => {
        if (!audioRef.current) return;
        audioRef.current.currentTime = Math.max(0, Math.min(seconds, audioRef.current.duration || 0));
        setCurrentTime(audioRef.current.currentTime);
        lastTimeRef.current = audioRef.current.currentTime;
    };

    const toggleFavorite = (songId) => setFavorites(p => ({ ...p, [songId]: !p[songId] }));
    const addToQueue = (song) => setQueue(p => [...p, song]);

    const rateSong = (songId, voteType) => {
        setRatings(prev => {
            const current = prev[songId];
            if (current === voteType) {
                const newState = { ...prev };
                delete newState[songId];
                return newState;
            }
            return { ...prev, [songId]: voteType };
        });
    };

    const toggleShuffle = () => setIsShuffleOn(s => !s);

    const toggleRepeat = () => {
        if (!isRepeatOn && !isRepeatOneOn) { setIsRepeatOn(true); setIsRepeatOneOn(false); }
        else if (isRepeatOn && !isRepeatOneOn) { setIsRepeatOneOn(true); setIsRepeatOn(false); }
        else { setIsRepeatOn(false); setIsRepeatOneOn(false); }
    };

    const toggleMute = () => {
        if (volume > 0) { setPreviousVolume(volume); setVolume(0); }
        else { setVolume(previousVolume || 0.5); }
    };

    const setVolumePercent = (pct) => {
        const v = Math.max(0, Math.min(100, Number(pct)));
        setVolume(v / 100);
        setPreviousVolume(v / 100);
    };

    const value = {
        currentSong, isPlaying, queue, history,
        playSong, pause, playNext, playPrev, addToQueue,
        seekTo, setVolumePercent, toggleMute, toggleShuffle, toggleRepeat,
        favorites, toggleFavorite, ratings, rateSong,
        currentTime, duration, uiVolumePercent: Math.round(volume * 100),
        isShuffleOn, isRepeatOn, isRepeatOneOn,
        viewUpdateTrigger,
        removeFromQueue: (index) => setQueue(q => q.filter((_, i) => i !== index))
    };

    return (
        <PlayerContext.Provider value={value}>
            <audio ref={audioRef} crossOrigin="use-credentials" style={{ display: 'none' }} />
            {children}
        </PlayerContext.Provider>
    );
}