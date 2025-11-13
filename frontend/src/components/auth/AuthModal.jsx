import React, {useEffect, useState} from "react";
import {useAuth} from "../../context/useAuth.js";
import './AuthModal.css';
import googleIcon from '../../assets/images/googleIcon.png';

function AuthModal() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
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
        loading: authLoading,
        error: authError
    } = useAuth();

    useEffect(() => {
        if (isModalOpen) {
            setEmail("");
            setPassword("");
            setErrors({email: "", password: "", general: ""});
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

        // window.location.href = "/";
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
                    <div className="auth-form">
                        <h2>Stwórz konto</h2>
                        <input type="text" placeholder="Nazwa użytkownika" />
                        <input type="email" placeholder="Email" />
                        <input type="password" placeholder="Hasło" />
                        <input type="password" placeholder="Powtórz hasło" />

                        <div className="auth-terms">
                            <input type="checkbox" id="terms" name="terms" />
                            <label htmlFor="terms">Zaakceptuj regulamin</label>
                        </div>

                        <button className="auth-submit-button">Zarejestruj</button>

                        <div className="auth-divider"><span>LUB</span></div>

                        <button className="google-login-button">
                            <img className="google-logo" src={googleIcon} alt="Rejestracja przez Google" />
                            Zarejestruj się przez Google
                        </button>

                        <p className="auth-switch">
                            Masz już konto?
                            <span onClick={switchToLogin}> Zaloguj się</span>
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default AuthModal;