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

export const getAlbumCoverUrl = (albumId) => {
    if (!albumId) return null;
    return `http://localhost:8080/api/albums/cover/${albumId}`;
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