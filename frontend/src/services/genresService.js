import api from "../context/axiosClient.js";

export default async function getGenres() {
    try {
        const { data } = await api.get("/genres");
        return data;
    } catch {
        return [
            "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
            "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
            "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
        ];
    }
}
