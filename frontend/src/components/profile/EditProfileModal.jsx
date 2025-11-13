import React, { useState } from 'react';
import ReactCrop, { centerCrop, makeAspectCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import './EditProfileModal.css';
import { useAuth } from '../../context/AuthContext';
import defaultAvatar from '../../assets/images/default-avatar.png';

function EditProfileModal({ isOpen, onClose }) {

    const { currentUser } = useAuth();

    // --- STANY DLA OBRAZKA (tak jak w AddSongModal) ---
    const [imgSrc, setImgSrc] = useState(currentUser?.avatar || defaultAvatar);
    const [crop, setCrop] = useState();
    // ------------------------------------------------

    if (!isOpen) {
        return null;
    }

    // --- FUNKCJE DLA OBRAZKA ---
    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setImgSrc('');
        const reader = new FileReader();
        reader.onload = () => {
            setImgSrc(reader.result.toString() || '');
            setCrop(centerCrop(
                makeAspectCrop({ unit: '%', width: 90 }, 1, 100, 100),
                100, 100
            ));
        };
        reader.readAsDataURL(file);
    };

    const removeCoverArt = () => {
        setImgSrc(defaultAvatar);
        document.getElementById('avatar-upload').value = null;
    };

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Edytuj profil</h2>

                <div className="edit-form">

                    {/* ===== KONTENER DLA 2 KOLUMN ===== */}
                    <div className="form-layout-container">

                        {/* ===== KOLUMNA LEWA (Zdjęcie i Bio) ===== */}
                        <div className="form-column-left">
                            <label>Zdjęcie profilowe</label>
                            <div className="cover-art-buttons">
                                <label htmlFor="avatar-upload" className="file-upload-button">
                                    Zmień zdjęcie
                                </label>
                                <input
                                    type="file"
                                    id="avatar-upload"
                                    className="file-upload-input"
                                    accept="image/png, image/jpeg"
                                    onChange={handleFileChange}
                                />
                                <button
                                    type="button"
                                    className="remove-image-button"
                                    onClick={removeCoverArt}
                                >
                                    Usuń
                                </button>
                            </div>

                            {/* Cropper (tak jak w AddSongModal) */}
                            <div className="cropper-container">
                                <ReactCrop
                                    crop={crop}
                                    onChange={c => setCrop(c)}
                                    aspect={1}
                                >
                                    <img src={imgSrc} alt="Podgląd awatara"/>
                                </ReactCrop>
                            </div>

                            <label htmlFor="bio">Bio</label>
                            <textarea
                                id="bio"
                                rows="4"
                                placeholder="Opisz siebie..."
                            />
                        </div>

                        {/* ===== KOLUMNA PRAWA (Dane) ===== */}
                        <div className="form-column-right">
                            <label htmlFor="username">Nazwa użytkownika</label>
                            <input type="text" id="username" placeholder="Twoja nowa nazwa" />

                            <label htmlFor="email">Zmień e-mail</label>
                            <input type="email" id="email" placeholder="nowy@email.com" />

                            <label htmlFor="password">Nowe hasło</label>
                            <input type="password" id="password" placeholder="••••••••" />

                            <label htmlFor="password-confirm">Powtórz nowe hasło</label>
                            <input type="password" id="password-confirm" placeholder="••••••••" />

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
                        </div>
                    </div>

                    <button className="save-button">Zapisz zmiany</button>
                </div>
            </div>
        </div>
    );
}

export default EditProfileModal;