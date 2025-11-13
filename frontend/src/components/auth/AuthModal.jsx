import React from "react";
import {useAuth} from "../../context/AuthContext.jsx";
import './AuthModal.css';
import googleIcon from '../../assets/images/googleIcon.png';
import defaultAvatar from '../../assets/images/default-avatar.png';

function AuthModal() {
    const {isModalOpen, closeModal, modalView, switchToLogin, switchToRegister, login} = useAuth();
    if(!isModalOpen) {
        return null;
    }
    return (
        <div className="auth-modal-backdrop" onClick={() => closeModal()}>
            <div className="auth-modal-content" onClick={(e ) => e.stopPropagation()}>
                <button className="close-button" onClick={() => closeModal()}>
                    X
                </button>
                {modalView === 'login' ? (
                    <div className="auth-form">
                        <h2>Zaloguj się</h2>
                        <input type="email" placeholder="Email" />
                        <input type="password" placeholder="Hasło" />
                        <button className="auth-submit-button" onClick={() => login({ avatar: defaultAvatar, name: 'Użytkownik' })}>
                            Zaloguj
                        </button>
                        <div className="auth-divider">
                            <span>LUB</span>
                        </div>

                        <button className="google-login-button">
                            <img className="google-logo" src={googleIcon} alt="Logowanie przez Google"/>
                            Zaloguj się przez Google
                        </button>
                        <p className="auth-switch">
                            Nie masz konta?
                            <span onClick={switchToRegister}> Zarejestruj się</span>
                        </p>
                    </div>
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
                        <div className="auth-divider">
                            <span>LUB</span>
                        </div>
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
    )
}
export default AuthModal;