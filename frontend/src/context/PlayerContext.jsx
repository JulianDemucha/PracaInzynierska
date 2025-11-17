import React, { createContext, useState, useContext } from 'react';

const PlayerContext = createContext();

// 2. Provider (komponent, który "trzyma" stan)
export function PlayerProvider({ children }) {
    // Stan przechowuje CAŁY obiekt piosenki, która teraz gra
    const [currentSong, setCurrentSong] = useState(null);
    // Stan przechowuje, czy odtwarzacz jest aktywny
    const [isPlaying, setIsPlaying] = useState(false);
    // Stan przechowuje listę piosenek w kolejce
    const [queue, setQueue] = useState([]);

    const playSong = (song, songList = null) => {
        // Jeśli to ta sama piosenka, po prostu wznów
        if (currentSong?.id === song.id) {
            setIsPlaying(true);
        } else {
            // Jeśli to nowa piosenka, ustaw ją i odtwórz
            setCurrentSong(song);
            setIsPlaying(true);

            // Jeśli podano całą listę (np. album), ustaw ją jako kolejkę
            if (songList) {
                // Znajdź indeks klikniętej piosenki i ustaw kolejkę od tego miejsca
                const songIndex = songList.findIndex(s => s.id === song.id);
                setQueue(songList.slice(songIndex + 1));
            } else {
                // Jeśli to pojedyncza piosenka, wyczyść kolejkę
                setQueue([]);
            }
        }
    };

    // Funkcja do pauzowania
    const pause = () => {
        setIsPlaying(false);
    };

    // Funkcja do dodawania do kolejki
    const addToQueue = (song) => {
        // Dodaj piosenkę na koniec tablicy 'queue'
        setQueue(prevQueue => [...prevQueue, song]);
    };

    // Wartości udostępniane całej aplikacji
    const value = {
        currentSong,
        isPlaying,
        queue,
        playSong,
        pause,
        addToQueue,
    };

    return (
        <PlayerContext.Provider value={value}>
            {children}
        </PlayerContext.Provider>
    );
}

// 3. Stwórz "hak" (skrót) do łatwego używania kontekstu
// (Ten komentarz wyłącza błąd ESLint, o którym rozmawialiśmy)
// eslint-disable-next-line react-refresh/only-export-components
export function usePlayer() {
    return useContext(PlayerContext);
}