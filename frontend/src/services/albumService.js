import api from "../context/axiosClient";

export const createAlbum = async (albumData) => {
    const response = await api.post('/albums/create', albumData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    })
    return response.data;
};

export const getUserAlbums = async (userId) => {
    const response = await api.get(`/albums/user/${userId}`);
    return response.data;
};

export const getAlbumById = async (id) => {
    const response = await api.get(`/albums/${id}`);
    return response.data;
};

export const getSongsByAlbumId = async (albumId) => {
    const response = await api.get(`/albums/${albumId}/songs`);
    return response.data;
};

export const deleteAlbum = async (albumId) => {
    const response = await api.delete(`/albums/${albumId}`);
    return response.data;
};

export const getAllAlbums = async () => {
    try {
        const response = await api.get('/albums');
        return response.data;
    } catch (error) {
        console.error("Błąd pobierania wszystkich albumów:", error);
        return [];
    }
};
export const getAlbumsByGenre = async (genreName) => {
    try {
        // Używamy nowego endpointu backendowego
        const response = await api.get(`/albums/genre/${genreName}`);
        return response.data;
    } catch (error) {
        console.error(`Błąd pobierania albumów z gatunku ${genreName}:`, error);
        return [];
    }
};

export const addSongToAlbum = async (albumId, songData) => {
    const response = await api.post(`/albums/${albumId}/add`, songData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    });
    return response.data;
};

export const removeSongFromAlbum = async (albumId, songId) => {
    const response = await api.delete(`/albums/${albumId}/remove/${songId}`);
    return response.data;
};