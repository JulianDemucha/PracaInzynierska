import api from "../context/axiosClient";

export const getUserPlaylists = async (userId) => {
    if (!userId) {
        console.error("Brak userId przy pobieraniu playlist!");
        return [];
    }
    const response = await api.get(`/playlists/user/${userId}`);
    return response.data;
};

export const createPlaylist = async (playlistData) => {
    const response = await api.post('/playlists/create', playlistData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    });
    return response.data;
};

export const getPlaylistById = async (playlistId) => {
    const response = await api.get(`/playlists/${playlistId}`);
    return response.data;
};

export const addSongToPlaylist = async (playlistId, songId) => {
    const response = await api.post(`/playlists/${playlistId}/add/${songId}`);
    return response.data;
};

export const getPlaylistSongs = async (playlistId) => {
    const response = await api.get(`/playlists/${playlistId}/songs`);
    return response.data;
};

export const deletePlaylist = async (playlistId) => {
    const response = await api.delete(`/playlists/${playlistId}`);
    return response.data; // Zwraca 204 No Content, więc data może być pusta
};

export const removeSongFromPlaylist = async (playlistId, songId) => {
    const response = await api.delete(`/playlists/${playlistId}/remove/${songId}`);
    return response.data;
};

export const changeSongPosition = async (playlistId, songId, newPosition) => {
    const response = await api.put(`/playlists/${playlistId}/changeSongPosition/${songId}/${newPosition}`);
    return response.data;
};

export const getAllPlaylists = async () => {
    const response = await api.get('/playlists');
    return response.data;
};

export const updatePlaylist = async (playlistId, playlistData) => {
    const response = await api.put(`/playlists/${playlistId}`, playlistData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    });
    return response.data;
}