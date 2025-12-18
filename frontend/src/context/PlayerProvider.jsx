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

    const [currentTime, setCurrentTime] = useState(0);
    const [duration, setDuration] = useState(0);
    const [volume, setVolume] = useState(0.5);
    const [previousVolume, setPreviousVolume] = useState(0.5);

    const [favorites, setFavorites] = useState({});
    const [ratings, setRatings] = useState({});
    const [isShuffleOn, setIsShuffleOn] = useState(false);
    const [isRepeatOn, setIsRepeatOn] = useState(false);
    const [isRepeatOneOn, setIsRepeatOneOn] = useState(false);

    const [viewUpdateTrigger, setViewUpdateTrigger] = useState(0);

    useEffect(() => {
        const audio = audioRef.current;
        if (!audio) return;

        const handleTimeUpdate = () => {
            const now = audio.currentTime || 0;
            setCurrentTime(now);

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

        const handleLoadedMetadata = () => {
            setDuration(isFinite(audio.duration) ? audio.duration : 0);
            if (currentSong && audio.paused) {
                audio.play().catch(e => console.warn("Autoplay blocked:", e));
            }
        };

        const handleEnded = () => {
            if (isRepeatOneOn) {
                audio.currentTime = 0;
                accumulatedTimeRef.current = 0;
                viewRegisteredRef.current = false;
                lastTimeRef.current = 0;
                audio.play().catch(console.error);
            } else {
                playNext();
            }
        };

        const handlePlay = () => {
            setIsPlaying(true);
            lastTimeRef.current = audio.currentTime;
        };
        const handlePause = () => setIsPlaying(false);
        const handleError = (e) => {
            console.error("Audio error:", e);
            setIsPlaying(false);
        };

        audio.addEventListener('timeupdate', handleTimeUpdate);
        audio.addEventListener('loadedmetadata', handleLoadedMetadata);
        audio.addEventListener('ended', handleEnded);
        audio.addEventListener('play', handlePlay);
        audio.addEventListener('pause', handlePause);
        audio.addEventListener('error', handleError);

        return () => {
            audio.removeEventListener('timeupdate', handleTimeUpdate);
            audio.removeEventListener('loadedmetadata', handleLoadedMetadata);
            audio.removeEventListener('ended', handleEnded);
            audio.removeEventListener('play', handlePlay);
            audio.removeEventListener('pause', handlePause);
            audio.removeEventListener('error', handleError);
        };
    }, [currentSong, isRepeatOneOn]);

    useEffect(() => {
        if (audioRef.current) {
            audioRef.current.volume = volume;
        }
    }, [volume]);

    useEffect(() => {
        const audio = audioRef.current;
        if (!audio) return;

        accumulatedTimeRef.current = 0;
        viewRegisteredRef.current = false;
        lastTimeRef.current = 0;

        if (!currentSong) {
            audio.pause();
            audio.src = "";
            return;
        }

        const newSrc = `/api/songs/stream/${currentSong.id}`;

        if (!audio.src || !audio.src.includes(`/api/songs/stream/${currentSong.id}`)) {
            audio.src = newSrc;
            audio.load();
            const playPromise = audio.play();
            if (playPromise !== undefined) {
                playPromise.catch(error => {
                    console.log("Play interrupted or waiting for interaction:", error);
                });
            }
        } else {
            audio.play().catch(console.error);
        }

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
            const audio = audioRef.current;
            if (audio) {
                if (audio.paused) audio.play().catch(console.error);
                else audio.pause();
            }
            return;
        }

        if (currentSong) setHistory(h => [...h, currentSong]);
        setCurrentSong(song);
    };

    const pause = () => {
        if (audioRef.current) audioRef.current.pause();
    };

    const playNext = () => {
        if (isShuffleOn && queue.length > 0) {
            const idx = Math.floor(Math.random() * queue.length);
            const nextSong = queue[idx];

            setQueue(prevQueue => {
                const newQueue = [...prevQueue];
                newQueue.splice(idx, 1);
                return newQueue;
            });

            if (currentSong) setHistory(h => [...h, currentSong]);
            setCurrentSong(nextSong);
            return;
        }

        if (queue.length > 0) {
            const [next, ...rest] = queue;
            setQueue(rest);
            if (currentSong) setHistory(h => [...h, currentSong]);
            setCurrentSong(next);
            return;
        }

        if (isRepeatOn && history.length > 0) {
            setIsPlaying(false);
        } else {
            setIsPlaying(false);
        }
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
        audioRef.current.currentTime = seconds;
        setCurrentTime(seconds);
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
        else { setVolume(previousVolume > 0 ? previousVolume : 0.5); }
    };

    const setVolumePercent = (pct) => {
        const v = Math.max(0, Math.min(100, Number(pct)));
        setVolume(v / 100);
        if (v > 0) setPreviousVolume(v / 100);
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
            <audio
                ref={audioRef}
                crossOrigin="use-credentials"
                style={{ display: 'none' }}
                preload="auto"
            />
            {children}
        </PlayerContext.Provider>
    );
}