import api from "../context/axiosClient.js";

// Jeśli masz to w zmiennych środowiskowych to użyj import.meta.env.VITE_API_URL, ale na sztywno też zadziała na start
const API_URL = "http://localhost:8080/api";

// 1. Pobieranie szczegółów piosenki
export const getSongById = async (id) => {
    const response = await api.get(`/songs/${id}`);
    return response.data;
};

// 2. Pobieranie piosenek użytkownika
export const getUserSongs = async (userId) => {
    const response = await api.get(`/songs/user/${userId}`);
    return response.data;
};

export const deleteSong = async (songId) => {
    const response = await api.delete(`/songs/${songId}`);
    return response.data;
};

export const getSongsByGenre = async (genreName) => {
    const response = await api.get(`/songs/genre/${genreName}`);
    return response.data;
};

export const getAllSongs = async () => {
    try {
        const response = await api.get('/songs');
        return response.data;
    } catch (error) {
        console.error("Błąd pobierania wszystkich utworów:", error);
        return [];
    }
};