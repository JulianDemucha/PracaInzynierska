import React, { createContext, useState, useContext } from 'react';

const PlayerContext = createContext();

export function PlayerProvider({ children }) {
    const [currentSong, setCurrentSong] = useState(null);
    const [isPlaying, setIsPlaying] = useState(false);
    const [queue, setQueue] = useState([]);

    const [favorites, setFavorites] = useState({});
    const [ratings, setRatings] = useState({});

    const playSong = (song, songList = null) => {
        if (currentSong?.id === song.id) {
            setIsPlaying(true);
            if (songList) {
                const songIndex = songList.findIndex(s => s.id === song.id);
                if (songIndex !== -1) {
                    setQueue(songList.slice(songIndex + 1));
                }
            }
        } else {
            setCurrentSong(song);
            setIsPlaying(true);

            if (songList) {
                const songIndex = songList.findIndex(s => s.id === song.id);
                setQueue(songList.slice(songIndex + 1));
            } else {
                setQueue([]);
            }
        }
    };

    const pause = () => {
        setIsPlaying(false);
    };

    const addToQueue = (song) => {
        setQueue(prevQueue => [...prevQueue, song]);
    };

    const toggleFavorite = (songId) => {
        setFavorites(prev => ({
            ...prev,
            [songId]: !prev[songId]
        }));
    };

    const rateSong = (songId, voteType) => {
        setRatings(prev => {
            const currentRating = prev[songId];
            if (currentRating === voteType) {
                const newState = { ...prev };
                delete newState[songId];
                return newState;
            }
            return {
                ...prev,
                [songId]: voteType
            };
        });
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
        rateSong
    };

    return (
        <PlayerContext.Provider value={value}>
            {children}
        </PlayerContext.Provider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function usePlayer() {
    return useContext(PlayerContext);
}