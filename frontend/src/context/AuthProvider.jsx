import {useState, useEffect} from "react";
import api from "./axiosClient.js";
import { AuthContext } from "./authContext.js";

export default function AuthProvider({children}) {
    const [currentUser, setCurrentUser] = useState(null)
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalView, setModalView] = useState('login');
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null);

    const openModal = (view = 'login') => {
        setModalView(view);
        setIsModalOpen(true);
    };
    const closeModal = () => { setIsModalOpen(false); };
    const switchToRegister = () => setModalView('register');
    const switchToLogin = () => setModalView('login');

    const fetchCurrentUser = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await api.get("/users/me");
            setCurrentUser(res.data);

        } catch (err) {
            setCurrentUser(null)
            if (err.response && err.response.status !== 401) {
                setError(err.response?.data?.message || "niespodziewany blad podczas sprawdzania 'sesji'");
            }

        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        void fetchCurrentUser(); //jak error to setCurrentUser(null)
    }, []);

    const login = async (credentials) => {
        setLoading(true);
        setError(null);
        try {
            await api.post("/auth/authenticate", credentials);
            await fetchCurrentUser();
            closeModal();
            return { ok: true };
        } catch (err) {
            const msg = err.response?.data?.message || "Authentication failed";
            setError(msg);
            setLoading(false);
            return { ok: false, error: msg };
        }
    };

    const register = async (registerData) => {
        setLoading(true);
        setError(null);
        try {
            await api.post("/auth/register", registerData);
            await fetchCurrentUser();
            closeModal();
            return { ok: true };
        } catch (err) {
            const msg = err.response?.data?.message || "Registration failed";
            setError(msg);
            setLoading(false);
            return { ok: false, error: msg };
        }
    };

    const logout = async () => {
            await api.post("/auth/logout");
            setCurrentUser(null);
    };

    const value = {
        isModalOpen,
        modalView,
        openModal,
        closeModal,
        switchToRegister,
        switchToLogin,
        currentUser,
        login,
        register,
        logout,
        fetchCurrentUser,
        loading,
        error,
        isAuthenticated: !!currentUser,
    }

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}

// TODO: jak przeczytasz to usun
// Komentarz wyłączający regułę ESLint dla tej linii
// eslint-disable-next-line react-refresh/only-export-components
// jaka sigma XD ten useauth wystarczy exportowac w innym pliku, nie ignoruj tak bledow
// export function useAuth() {
//     return useContext(AuthContext);
// }
