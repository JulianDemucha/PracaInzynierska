import {createContext, useState, useContext} from "react";

const AuthContext = createContext()
export default function AuthProvider({ children }) {
    const[isModalOpen, setIsModalOpen] = useState(false);
    const[modalView, setModalView] = useState('login');
    const openModal = (view = 'login') => {
        setModalView(view);
        setIsModalOpen(true);
    };
    const closeModal = () => {
        isModalOpen(false);
    };
    const switchToRegister = () => setModalView('register');
    const switchToLogin = () => setModalView('login');

    const value = {
        isModalOpen,
        modalView,
        openModal,
        closeModal,
        switchToRegister,
        switchToLogin,
    }
    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}
// Komentarz wyłączający regułę ESLint dla tej linii
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
    return useContext(AuthContext);
}
