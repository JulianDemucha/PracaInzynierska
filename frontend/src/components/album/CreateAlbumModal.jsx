import React, {useState} from 'react';
import ReactCrop, {centerCrop, makeAspectCrop} from "react-image-crop";
import 'react-image-crop/dist/ReactCrop.css';
import './CreateAlbumModal.css';
import defaultAvatar from '../../assets/images/default-avatar.png';

function CreateAlbumModal({isOpen, onClose}) {

    const [imgSrc, setImgSrc] = useState(defaultAvatar);
    const [crop, setCrop] = useState();

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
                makeAspectCrop({unit: '%', width: 90}, 1, 100, 100),
                100, 100
            ));
        };
        reader.readAsDataURL(file);
    };

    const removeCoverArt = () => {
        setImgSrc(defaultAvatar);
        const input = document.getElementById('album-cover-upload');
        if (input) input.value = null;
    };

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Wydaj nowy album</h2>

                <div className="edit-form">
                    {/* Nazwa albumu */}
                    <label htmlFor="album-name">Nazwa albumu</label>
                    <input type="text" id="album-name" placeholder="Nazwa albumu" />
                    {/* Okładka albumu */}
                    <label>Okładka albumu</label>
                    <div className="cover-art-buttons">
                        <label htmlFor="album-cover-upload" className="file-upload-button">
                            Zmień okładkę
                        </label>
                        <input
                            type="file"
                            id="album-cover-upload"
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

                    {/* Cropper */}
                    <div className="cropper-container">
                        <ReactCrop
                            crop={crop}
                            onChange={c => setCrop(c)}
                            aspect={1}
                        >
                            <img src={imgSrc} alt="Podgląd okładki"/>
                        </ReactCrop>
                    </div>
                    <fieldset className="visibility-selection">
                        <legend>Widoczność</legend>
                        <div className="visibility-option">
                            <input type="radio" id="public" name="visibility" value="PUBLIC" defaultChecked />
                            <label htmlFor="public">Publiczny</label>
                        </div>
                        <div className="visibility-option">
                            <input type="radio" id="private" name="visibility" value="PRIVATE" />
                            <label htmlFor="private">Prywatny</label>
                        </div>
                    </fieldset>

                    {/*  SEKCJA PRZYCISKÓW "Stwórz" */}
                    <div className="form-actions">
                        <button className="save-button">Stwórz album</button>
                    </div>
                </div>
            </div>
        </div>

    )
}
export default CreateAlbumModal;