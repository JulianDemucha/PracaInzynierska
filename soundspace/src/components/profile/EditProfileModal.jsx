import React from 'react';
import './EditProfileModal.css';
import { useAuth } from '../../context/AuthContext';
import defaultAvatar from '../../assets/images/default-avatar.png';

function EditProfileModal({ isOpen, onClose }) {

    const { currentUser } = useAuth();
    if (!isOpen) {
        return null;
    }

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Edytuj profil</h2>

                <div className="edit-form">

                    {/* ===== ZDJĘCIE PROFILOWE (NOWA SEKCJA) ===== */}
                    <div className="avatar-edit-section">
                        <img
                            src={currentUser?.avatar || defaultAvatar}
                            alt="Obecny awatar"
                            className="current-avatar-preview"
                        />
                        <div className="avatar-edit-actions">
                            <label htmlFor="avatar-upload" className="file-upload-button">
                                Zmień zdjęcie
                            </label>
                            <input
                                type="file"
                                id="avatar-upload"
                                className="file-upload-input"
                                accept="image/png, image/jpeg"
                            />
                            <p className="file-hint">PNG lub JPG, maks. 2MB.</p>
                        </div>
                    </div>

                    {/* ===== RESZTA FORMULARZA ===== */}
                    <label htmlFor="username">Nazwa użytkownika</label>
                    <input type="text" id="username" placeholder="Twoja nowa nazwa" />

                    <label htmlFor="bio">Bio</label>
                    <textarea id="bio" rows="3" placeholder="Opisz siebie..."></textarea>

                    <label htmlFor="email">Zmień e-mail</label>
                    <input type="email" id="email" placeholder="nowy@email.com" />

                    <label htmlFor="password">Nowe hasło</label>
                    <input type="password" id="password" placeholder="••••••••" />

                    <label htmlFor="password-confirm">Powtórz nowe hasło</label>
                    <input type="password" id="password-confirm" placeholder="••••••••" />

                    {/* ===== PŁEĆ ===== */}
                    <fieldset className="gender-selection">
                        <legend>Płeć</legend>
                        <div className="gender-option">
                            <input type="radio" id="male" name="gender" value="male" />
                            <label htmlFor="male">Mężczyzna</label>
                        </div>
                        <div className="gender-option">
                            <input type="radio" id="female" name="gender" value="female" />
                            <label htmlFor="female">Kobieta</label>
                        </div>
                    </fieldset>

                    <button className="save-button">Zapisz zmiany</button>
                </div>
            </div>
        </div>
    );
}

export default EditProfileModal;