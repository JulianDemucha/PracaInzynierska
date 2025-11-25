import api from "../context/axiosClient";

// Tworzenie albumu (Wysyłamy JSON, bo Controller ma @RequestBody)
export const createAlbum = async (albumData) => {
    // albumData to obiekt: { title, description, publicVisible, authorId }
    const response = await api.post('/albums/create', albumData, {
        headers: { "Content-Type": "application/json" }
    });
    return response.data;
};

// Dodawanie piosenki do albumu
export const addSongToAlbum = async (albumId, songId) => {
    const response = await api.post(`/albums/${albumId}/add/${songId}`);
    return response.data;
};

// Pobieranie albumu (opcjonalnie)
export const getAlbum = async (id) => {
    const response = await api.get(`/albums/${id}`);
    return response.data;
};