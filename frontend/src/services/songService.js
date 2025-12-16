import api from "../context/axiosClient.js";

const API_URL = "http://localhost:8080/api";

export const getSongById = async (id) => {
    const response = await api.get(`/songs/${id}`);
    return response.data;
};

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

export const updateSong = async (songId, songData) => {
    const response = await api.put(`/songs/${songId}`, songData, {
        headers: {
            "content-type": "multipart/form-data",
        },
    });
    return response.data;
};

export const likeSong = async (songId) => {
    const response = await api.post(`/songs/${songId}/like`);
    return response.data;
};

export const dislikeSong = async (songId) => {
    const response = await api.post(`/songs/${songId}/dislike`);
    return response.data;
};

export const removeLike = async (songId) => {
    const response = await api.delete(`/songs/${songId}/like`);
    return response.data;
};

export const removeDislike = async (songId) => {
    const response = await api.delete(`/songs/${songId}/dislike`);
    return response.data;
};

export const addSongToFavorites = async (songId) => {
    const response = await api.post(`/songs/${songId}/favourite`);
    return response.data;
};

export const removeSongFromFavorites = async (songId) => {
    const response = await api.delete(`/songs/${songId}/favourite`);
    return response.data;
};

export const registerView = async (songId) => {
    const response = await api.post(`/songs/${songId}/registerView`);
    return response.data;
};

export const getFavouriteSongs = async (page = 0, size = 20) => {
    const response = await api.get(`/favourites?page=${page}&size=${size}`);
    return response.data;
};