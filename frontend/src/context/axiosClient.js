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
    Interceptor odpowiedzi:
    Gdy backend zwróci 401 UNAUTHORIZED (brak tokena lub expired),
    próbuje odświeżyć token (refresh token) i ponowić zapytanie.
 */
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            // Jeśli odświeżanie już trwa, dodaj zapytanie do kolejki
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(() => api(originalRequest));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                await axios.post("/api/auth/refreshToken", {}, { withCredentials: true });
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
