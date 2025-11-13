
import axios from "axios";

const api = axios.create({
    baseURL: "/api",
    withCredentials: true, //httponly true
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, result) => {
    failedQueue.forEach(p => {
        if (error) p.reject(error);
        else p.resolve(result);
    });
    failedQueue = [];
};

/*
    jak response z backendu == 401 UNAUTHORIZED (czyli nie ma tokena jwt, albo jest expired)
    to tu sie odpala refresh refreshtokena ktory ustawia nowy jwt
 */
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            // to na wszelki wypadek jakby refresh sie probowal zrobic jak juz jeden trwa
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(() => api(originalRequest));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                await axios.post("/api/auth/refresh", {}, { withCredentials: true });
                isRefreshing = false;
                processQueue(null, true);
                return api(originalRequest);
            } catch (err) {
                isRefreshing = false;
                processQueue(err, null);
                return Promise.reject(err);
            }
        }

        return Promise.reject(error);
    }
);

export default api;
