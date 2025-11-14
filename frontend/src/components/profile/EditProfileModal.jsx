import React, {useEffect, useRef, useState} from 'react';
import ReactCrop, {centerCrop, makeAspectCrop} from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import './EditProfileModal.css';
import {useAuth} from "../../context/useAuth.js";
import defaultAvatar from '../../assets/images/default-avatar.png';
import api from "../../context/axiosClient.js";

function EditProfileModal({isOpen, onClose}) {
    const {currentUser, fetchCurrentUser} = useAuth();

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [bio, setBio] = useState("");
    const [sex, setSex] = useState("OTHER");
    const [password, setPassword] = useState("");
    const [passwordConfirm, setPasswordConfirm] = useState("");

    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");

    const [imgSrc, setImgSrc] = useState(currentUser?.avatar || defaultAvatar);
    const [crop, setCrop] = useState();
    const imgRef = useRef(null);

    useEffect(() => {
        if (isOpen && currentUser) {
            setUsername(currentUser.login ?? "");
            setEmail(currentUser.email ?? "");
            setBio(currentUser.bio ?? "");
            setSex(currentUser.sex ?? "OTHER");
            setPassword("");
            setPasswordConfirm("");

            setImgSrc(currentUser?.avatarUrl ?? defaultAvatar); // przyjmujemy pole avatarUrl w DTO
            setCrop(centerCrop(makeAspectCrop({unit: '%', width: 90}, 1, 100, 100), 100, 100));
            setErrors({});
            setSuccessMessage("");
        }
    }, [isOpen, currentUser]);

    if (!isOpen) {
        return null;
    }

    const validateEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

    // --- FUNKCJE DLA OBRAZKA ---
    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setImgSrc('');
        const reader = new FileReader();
        reader.onload = () => {
            setImgSrc(reader.result.toString() || '');
            setCrop(centerCrop(
                makeAspectCrop({unit: '%', width: 90}, 1, 100, 100),
                100, 100
            ));
        };
        reader.readAsDataURL(file);
    };

    const removeAvatarPreview = () => {
        setImgSrc(defaultAvatar);
        const input = document.getElementById('avatar-upload');
        if (input) input.value = null;
    };

    // todo: zintegrowac z backendem avatarimage
    // const getCroppedImgBlob = async (imageEl, cropObj) => {
    //     if (!cropObj || !imageEl) return null;
    //
    //     const canvas = document.createElement('canvas');
    //     const scaleX = imageEl.naturalWidth / imageEl.width;
    //     const scaleY = imageEl.naturalHeight / imageEl.height;
    //
    //     const pixelCrop = {
    //         x: cropObj.x * scaleX,
    //         y: cropObj.y * scaleY,
    //         width: (cropObj.width ?? 0) * scaleX,
    //         height: (cropObj.height ?? 0) * scaleY,
    //     };
    //
    //     canvas.width = pixelCrop.width;
    //     canvas.height = pixelCrop.height;
    //     const ctx = canvas.getContext('2d');
    //
    //     ctx.drawImage(
    //         imageEl,
    //         pixelCrop.x,
    //         pixelCrop.y,
    //         pixelCrop.width,
    //         pixelCrop.height,
    //         0,
    //         0,
    //         pixelCrop.width,
    //         pixelCrop.height
    //     );
    //
    //     return await new Promise((resolve) => {
    //         canvas.toBlob((blob) => resolve(blob), 'image/jpeg', 0.9);
    //     });
    // };

    // const uploadAvatar = async () => {
    //     if (!imgRef.current || !crop) return null;
    //     const blob = await getCroppedImgBlob(imgRef.current, crop);
    //     if (!blob) return null;
    //
    //     const fd = new FormData();
    //     fd.append("avatar", blob, "avatar.jpg");
    //
    //
    //     const res = await api.post("/users/me/avatar", fd, {
    //         headers: { "Content-Type": "multipart/form-data" },
    //     });
    //     return res;
    // };

    const handleSave = async (e) => {
        e.preventDefault();
        setErrors({});
        setSuccessMessage("");

        // podstawowa walidacja
        const newErr = {};
        if (!username || username.length < 3 || username.length > 16) newErr.username = "Nazwa użytkownika 3-16 znaków.";
        if (!validateEmail(email)) newErr.email = "Nieprawidłowy email.";
        if (password && (password.length < 8 || password.length > 24)) newErr.password = "Hasło 8-24 znaków.";
        if (password && password !== passwordConfirm) newErr.passwordConfirm = "Hasła nie są zgodne.";

        if (Object.keys(newErr).length) {
            setErrors(newErr);
            return;
        }

        setLoading(true);
        try {
            // DTO: { username, email, password, bio, sex }
            const payload = {
                username,
                email,
                password: password || null,
                bio,
                sex
            };

            await api.put("/users/me", payload);
            // opcjonalnie upload avatara jeśli użytkownik wybrał nowy obraz
            // const avatarResp = await uploadAvatar(); // odkomentuj jeśli backend obsługuje endpoint

            // odśwież aktualnego usera w kontekście
            await fetchCurrentUser();

            setSuccessMessage("Profil zaktualizowany pomyślnie.");
            setPassword("");
            setPasswordConfirm("");
            // zamknij modal po krótkim delay lub natychmiast:
            // onClose();
        } catch (err) {
            const msg = err?.response?.data || err.message || "Błąd podczas zapisu";
            // spróbuj sparsować message
            setErrors({general: typeof msg === "string" ? msg : (msg.message || JSON.stringify(msg))});
        } finally {
            setLoading(false);
        }
    };


    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Edytuj profil</h2>

                <form className="edit-form" onSubmit={handleSave}>

                    {/* ===== KONTENER DLA 2 KOLUMN ===== */}
                    <div className="form-layout-container">

                        {/* ===== KOLUMNA LEWA (Zdjęcie i Bio) ===== */}
                        <div className="form-column-left">
                            <label>Zdjęcie profilowe</label>
                            <div className="cover-art-buttons">
                                <label htmlFor="avatar-upload" className="file-upload-button">Zmień zdjęcie</label>
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
                                    onClick={removeAvatarPreview}
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
                                    <img ref={imgRef} src={imgSrc || defaultAvatar} alt="Podgląd awatara"/>
                                </ReactCrop>
                            </div>

                            <label htmlFor="bio">Bio</label>
                            <textarea
                                id="bio"
                                rows="4"
                                value={bio}
                                onChange={(e) => setBio(e.target.value)}/>

                        </div>

                        {/* ===== KOLUMNA PRAWA (Dane) ===== */}
                        <div className="form-column-right">
                            <label htmlFor="username">Nazwa użytkownika</label>
                            <label htmlFor="username">Nazwa użytkownika</label>
                            <input id="username" type="text" value={username} onChange={(e) => setUsername(e.target.value)} />
                            {errors.username && <div className="field-error">{errors.username}</div>}

                            <label htmlFor="email">Zmień e-mail</label>
                            <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                            {errors.email && <div className="field-error">{errors.email}</div>}

                            <label htmlFor="password">Nowe hasło</label>
                            <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
                            {errors.password && <div className="field-error">{errors.password}</div>}

                            <label htmlFor="password-confirm">Powtórz nowe hasło</label>
                            <input id="password-confirm" type="password" value={passwordConfirm} onChange={(e) => setPasswordConfirm(e.target.value)} />
                            {errors.passwordConfirm && <div className="field-error">{errors.passwordConfirm}</div>}

                            <fieldset className="gender-selection">
                                <legend>Płeć</legend>
                                <div className="gender-option">
                                    <input type="radio" id="male" name="gender" value="MALE" checked={sex === "MALE"} onChange={(e) => setSex(e.target.value)} />
                                    <label htmlFor="male">Mężczyzna</label>
                                </div>
                                <div className="gender-option">
                                    <input type="radio" id="female" name="gender" value="FEMALE" checked={sex === "FEMALE"} onChange={(e) => setSex(e.target.value)} />
                                    <label htmlFor="female">Kobieta</label>
                                </div>
                                <div className="gender-option">
                                    <input type="radio" id="other" name="gender" value="OTHER" checked={sex === "OTHER"} onChange={(e) => setSex(e.target.value)} />
                                    <label htmlFor="other">Inna/nie chcę podawać</label>
                                </div>
                            </fieldset>
                        </div>
                    </div>

                    {errors.general && <div className="field-error">{errors.general}</div>}
                    {successMessage && <div className="success-message">{successMessage}</div>}

                    <button className="save-button" type="submit" disabled={loading}>
                        {loading ? "Zapisywanie..." : "Zapisz zmiany"}
                    </button>
                </form>
            </div>
        </div>
    );
}

export default EditProfileModal;