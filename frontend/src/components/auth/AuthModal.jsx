import React, {useEffect, useState} from "react";
import {useAuth} from "../../context/useAuth.js";
import './AuthModal.css';
import googleIcon from '../../assets/images/googleIcon.png';

function AuthModal() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [sex, setSex] = useState("MALE");
    const [errors, setErrors] = useState({
        email: "",
        password: "",
        general: ""
    });
    const {
        isModalOpen,
        closeModal,
        modalView,
        switchToLogin,
        switchToRegister,
        login,
        register,
        loading: authLoading,
        error: authError
    } = useAuth();

    useEffect(() => {
        if (isModalOpen) {
            setEmail("");
            setPassword("");
            setConfirmPassword("");
            setUsername("");
            setSex("MALE");
            setErrors({username: "", email: "", password: "", confirmPassword: "", general: ""});
        }
    }, [isModalOpen, modalView]);

    useEffect(() => {
        if (authError) {
            setErrors(prev => ({...prev, general: authError}));
        }
    }, [authError]);

    if (!isModalOpen) {
        return null;
    }

    const validateEmail = (value) => {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!email || !password) {
            setErrors(prev => ({ ...prev, general: "Wypełnij wymagane pola." }));
            return;
        }

        if (!validateEmail(email)) {
            setErrors(prev => ({ ...prev, email: "Nieprawidłowy adres e-mail." }));
            return;
        }

        const result = await login({ email, password });

        if (!result.ok) {
            setErrors(prev => ({ ...prev, general: result.error || "Błąd logowania." }));
            return;
        }

    };

    const handleSubmitRegister = async (e) => {
        e.preventDefault();

        const newErrors = {username: "", email: "", password: "", confirmPassword: "", general: ""};
        if (!username) newErrors.username = "Wprowadź nazwę użytkownika.";
        if (!email || !validateEmail(email)) newErrors.email = "Podaj poprawny email.";
        if (!password || password.length < 6) newErrors.password = "Hasło musi mieć co najmniej 6 znaków.";
        if (password !== confirmPassword) newErrors.confirmPassword = "Hasła nie są zgodne.";

        setErrors(newErrors);

        if (newErrors.username || newErrors.email || newErrors.password || newErrors.confirmPassword) {
            return;
        }

        const result = await register({ username, email, password, sex });

        if (!result.ok) {
            setErrors(prev => ({ ...prev, general: result.error || "Błąd rejestracji." }));
            return;
        }
    };

    return (
        <div className="auth-modal-backdrop" onClick={() => closeModal()}>
            <div className="auth-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={() => closeModal()}>X</button>

                {modalView === 'login' ? (
                    <form className="auth-form" onSubmit={handleSubmit} noValidate>
                        <h2>Zaloguj się</h2>

                        <input
                            type="email"
                            placeholder="Email"
                            value={email}
                            onChange={(e) => {
                                setEmail(e.target.value);
                                setErrors(prev => ({ ...prev, email: "" }));
                            }}
                            required
                            aria-describedby="email-error"
                        />
                        {errors.email && <div id="email-error" className="field-error">{errors.email}</div>}

                        <input
                            type="password"
                            id="password"
                            placeholder="••••••••"
                            value={password}
                            onChange={(e) => {
                                setPassword(e.target.value);
                                setErrors(prev => ({ ...prev, password: "" }));
                            }}
                            required
                            aria-describedby="password-error"
                        />
                        {errors.password && <div id="password-error">{errors.password}</div>}

                        {errors.general && <div>{errors.general}</div>}

                        <button className="auth-submit-button" type="submit" disabled={authLoading}>
                            {authLoading ? "Logowanie..." : "Zaloguj"}
                        </button>

                        <div className="auth-divider"><span>LUB</span></div>

                        <button type="button" className="google-login-button">
                            <img className="google-logo" src={googleIcon} alt="Logowanie przez Google" />
                            Zaloguj się przez Google
                        </button>

                        <p className="auth-switch">
                            Nie masz konta?
                            <span onClick={switchToRegister}> Zarejestruj się</span>
                        </p>
                    </form>
                ) : (
                    <form className="auth-form" onSubmit={handleSubmitRegister} noValidate>
                        <h2>Stwórz konto</h2>

                        <input
                            type="text"
                            placeholder="Nazwa użytkownika"
                            value={username}
                            onChange={(e) => {
                                setUsername(e.target.value);
                                setErrors(prev => ({ ...prev, username: "" }));
                            }}
                            required
                        />
                        {errors.username && <div className="field-error">{errors.username}</div>}

                        <input
                            type="email"
                            placeholder="Email"
                            value={email}
                            onChange={(e) => {
                                setEmail(e.target.value);
                                setErrors(prev => ({ ...prev, email: "" }));
                            }}
                            required
                        />
                        {errors.email && <div className="field-error">{errors.email}</div>}

                        <input
                            type="password"
                            placeholder="Hasło"
                            value={password}
                            onChange={(e) => {
                                setPassword(e.target.value);
                                setErrors(prev => ({ ...prev, password: "" }));
                            }}
                            required
                        />
                        {errors.password && <div className="field-error">{errors.password}</div>}

                        <input
                            type="password"
                            placeholder="Powtórz hasło"
                            value={confirmPassword}
                            onChange={(e) => {
                                setConfirmPassword(e.target.value);
                                setErrors(prev => ({ ...prev, confirmPassword: "" }));
                            }}
                            required
                        />
                        {errors.confirmPassword && <div className="field-error">{errors.confirmPassword}</div>}

                        <label htmlFor="sex">Płeć</label>
                        <select id="sex" value={sex} onChange={(e) => setSex(e.target.value)}>
                            <option value="MALE">Mężczyzna</option>
                            <option value="FEMALE">Kobieta</option>
                            {/*<option value="OTHER">Inna/nie chcę podawać</option>*/} {/* dodac jeszcze 74 opcje w backendzie a pozniej zintegrowac */}
                        </select>

                        {errors.general && <div className="field-error">{errors.general}</div>}

                        <button className="auth-submit-button" type="submit" disabled={authLoading}>
                            {authLoading ? "Rejestracja..." : "Zarejestruj"}
                        </button>

                        <div className="auth-divider"><span>LUB</span></div>

                        <button type="button" className="google-login-button" disabled>
                            <img className="google-logo" src={googleIcon} alt="Rejestracja przez Google" />
                            Zarejestruj się przez Google
                        </button>

                        <p className="auth-switch">
                            Masz już konto?
                            <span onClick={switchToLogin}> Zaloguj się</span>
                        </p>
                    </form>
                )}
            </div>
        </div>
    );
}

export default AuthModal;