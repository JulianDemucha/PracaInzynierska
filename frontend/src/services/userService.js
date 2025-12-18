import api from "../context/axiosClient.js";


export const deleteUserAccount = async () => {
    const response = await api.delete('/users/me');
    return response.data;
};

export const deleteUserById = async (userId) => {
    const response = await api.delete(`/users/${userId}`);
    return response.data;
};