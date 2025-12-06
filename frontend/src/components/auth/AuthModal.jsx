import React, { useEffect, useState } from "react";
import { useAuth } from "../../context/useAuth.js";
import googleIcon from '../../assets/images/googleIcon.png';
import './AuthModal.css';

function AuthModal() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [sex, setSex] = useState("MALE");

    const [errors, setErrors] = useState({
        username: "",
        email: "",
        password: "",
        confirmPassword: "",
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
            setErrors({ username: "", email: "", password: "", confirmPassword: "", general: "" });
        }
    }, [isModalOpen, modalView]);

    useEffect(() => {
        if (authError) {
            setErrors(prev => ({ ...prev, general: authError }));
        }
    }, [authError]);

    if (!isModalOpen) {
        return null;
    }

    const validateEmail = (value) => {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
    };

    const handleSubmitLogin = async (e) => {
        e.preventDefault();

        let newErrors = { email: "", password: "", general: "" };
        let hasError = false;

        if (!email) {
            newErrors.email = "Wpisz adres email.";
            hasError = true;
        } else if (!validateEmail(email)) {
            newErrors.email = "Nieprawidłowy format email.";
            hasError = true;
        }

        if (!password) {
            newErrors.password = "Wpisz hasło.";
            hasError = true;
        }

        if (hasError) {
            setErrors(prev => ({ ...prev, ...newErrors }));
            return;
        }

        const result = await login({ email, password });
        if (!result.ok) {
            setErrors(prev => ({ ...prev, general: result.error || "Błąd logowania." }));
        }
    };

    const handleSubmitRegister = async (e) => {
        e.preventDefault();

        const newErrors = { username: "", email: "", password: "", confirmPassword: "", general: "" };
        let hasError = false;

        if (!username.trim()) {
            newErrors.username = "Wprowadź nazwę użytkownika.";
            hasError = true;
        }

        if (!email) {
            newErrors.email = "Podaj email.";
            hasError = true;
        } else if (!validateEmail(email)) {
            newErrors.email = "Podaj poprawny email.";
            hasError = true;
        }

        if (!password || password.length < 6) {
            newErrors.password = "Hasło musi mieć min. 6 znaków.";
            hasError = true;
        }

        if (password !== confirmPassword) {
            newErrors.confirmPassword = "Hasła nie są zgodne.";
            hasError = true;
        }

        setErrors(newErrors);

        if (hasError) return;

        const result = await register({ username, email, password, sex });
        if (!result.ok) {
            setErrors(prev => ({ ...prev, general: result.error || "Błąd rejestracji." }));
        }
    };

    const clearError = (field) => {
        setErrors(prev => ({ ...prev, [field]: "", general: "" }));
    };

    return (
        <div className="auth-modal-backdrop" onClick={() => closeModal()}>
            <div className="auth-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={() => closeModal()}>X</button>

                {modalView === 'login' ? (
                    <form className="auth-form" onSubmit={handleSubmitLogin} noValidate>
                        <h2>Zaloguj się</h2>

                        {errors.general && <div className="general-error">{errors.general}</div>}

                        <input
                            type="email"
                            placeholder="Email"
                            value={email}
                            className={errors.email ? "input-error" : ""}
                            onChange={(e) => {
                                setEmail(e.target.value);
                                clearError('email');
                            }}
                        />
                        {errors.email && <div className="field-error">{errors.email}</div>}

                        <input
                            type="password"
                            placeholder="Hasło"
                            value={password}
                            className={errors.password ? "input-error" : ""}
                            onChange={(e) => {
                                setPassword(e.target.value);
                                clearError('password');
                            }}
                        />
                        {errors.password && <div className="field-error">{errors.password}</div>}

                        <button className="auth-submit-button" type="submit" disabled={authLoading}>
                            {authLoading ? "Logowanie..." : "Zaloguj"}
                        </button>

                        <div className="auth-divider"><span>LUB</span></div>

                        <button type="button" className="google-login-button">
                            <img className="google-logo" src={googleIcon} alt="G" />
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

                        {errors.general && <div className="general-error">{errors.general}</div>}

                        <input
                            type="text"
                            placeholder="Nazwa użytkownika"
                            value={username}
                            className={errors.username ? "input-error" : ""}
                            onChange={(e) => {
                                setUsername(e.target.value);
                                clearError('username');
                            }}
                        />
                        {errors.username && <div className="field-error">{errors.username}</div>}

                        <input
                            type="email"
                            placeholder="Email"
                            value={email}
                            className={errors.email ? "input-error" : ""}
                            onChange={(e) => {
                                setEmail(e.target.value);
                                clearError('email');
                            }}
                        />
                        {errors.email && <div className="field-error">{errors.email}</div>}

                        <input
                            type="password"
                            placeholder="Hasło"
                            value={password}
                            className={errors.password ? "input-error" : ""}
                            onChange={(e) => {
                                setPassword(e.target.value);
                                clearError('password');
                            }}
                        />
                        {errors.password && <div className="field-error">{errors.password}</div>}

                        <input
                            type="password"
                            placeholder="Powtórz hasło"
                            value={confirmPassword}
                            className={errors.confirmPassword ? "input-error" : ""}
                            onChange={(e) => {
                                setConfirmPassword(e.target.value);
                                clearError('confirmPassword');
                            }}
                        />
                        {errors.confirmPassword && <div className="field-error">{errors.confirmPassword}</div>}

                        <label htmlFor="sex" className="sex-label">Płeć</label>
                        <select
                            id="sex"
                            value={sex}
                            onChange={(e) => setSex(e.target.value)}
                        >
                            <option value="MALE">Mężczyzna</option>
                            <option value="FEMALE">Kobieta</option>
                        </select>

                        <button className="auth-submit-button" type="submit" disabled={authLoading}>
                            {authLoading ? "Rejestracja..." : "Zarejestruj"}
                        </button>

                        <div className="auth-divider"><span>LUB</span></div>

                        <button type="button" className="google-login-button" disabled>
                            <img className="google-logo" src={googleIcon} alt="G" />
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