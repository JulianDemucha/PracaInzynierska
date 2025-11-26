import defaultAvatar from "../assets/images/default-avatar.png";

const API_URL = "http://localhost:5173/api";

export const getImageUrl = (storageKeyId) => {
    if (!storageKeyId) return defaultAvatar;
    return `${API_URL}/images/${storageKeyId}`;
}