import defaultAvatar from "../assets/images/default-avatar.png";

export const getImageUrl = (storageKeyId) => {
    if (!storageKeyId) return defaultAvatar;
    return `/api/images/${storageKeyId}`;
}