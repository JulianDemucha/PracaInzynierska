import api from "../context/axiosClient.js";

export const searchSongs = async (query, page = 0, size = 10) => {
    const response = await api.get(`/songs/search`, {
        params: { query, page, size }
    });
    return response.data;
};

export const searchAlbums = async (query, page = 0, size = 10) => {
    const response = await api.get(`/albums/search`, {
        params: { query, page, size }
    });
    return response.data;
};

export const searchPlaylists = async (query, page = 0, size = 10) => {
    const response = await api.get(`/playlists/search`, {
        params: { query, page, size }
    });
    return response.data;
};

export const searchUsers = async (query, page = 0, size = 10) => {
    const response = await api.get(`/users/search`, {
        params: { query, page, size }
    });
    return response.data;
};

export const searchAll = async (query) => {
    const [songs, albums, playlists, users] = await Promise.all([
        searchSongs(query, 0, 5),
        searchAlbums(query, 0, 5),
        searchPlaylists(query, 0, 5),
        searchUsers(query, 0, 5)
    ]);

    return {
        songs: songs.content,
        albums: albums.content,
        playlists: playlists.content,
        users: users.content
    };
};