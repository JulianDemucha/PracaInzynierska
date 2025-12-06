import React, { useEffect, useRef, useState } from 'react';
import ReactCrop, { centerCrop, makeAspectCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import { updateSong } from '../../services/songService.js';
import './AddSongModal.css';

function EditSongModal({ isOpen, onClose, songToEdit, onSongUpdated }) {
    const [title, setTitle] = useState("");
    const [isPublic, setIsPublic] = useState(false);
    const [imgSrc, setImgSrc] = useState('');
    const [crop, setCrop] = useState();
    const [isNewImageSelected, setIsNewImageSelected] = useState(false);
    const imgRef = useRef(null);
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [showValidation, setShowValidation] = useState(false);

    const isAlbumSong = Boolean(songToEdit?.albumId);

    useEffect(() => {
        if (isOpen && songToEdit) {
            setTitle(songToEdit.title || "");
            const pubVisible = songToEdit.publiclyVisible === true || songToEdit.publiclyVisible === "true";
            setIsPublic(pubVisible);
            setImgSrc('');
            setIsNewImageSelected(false);
            setErrorMessage("");
            setShowValidation(false);
        }
    }, [isOpen, songToEdit]);

    function onImageLoad(e) {
        const { width, height } = e.currentTarget;
        const smallestDimension = Math.min(width, height);
        const cropSize = smallestDimension * 0.9;
        const crop = centerCrop(
            makeAspectCrop({ unit: 'px', width: cropSize }, 1, width, height),
            width,
            height
        );
        setCrop(crop);
    }

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        setErrorMessage("");
        setCrop(undefined);
        setImgSrc('');
        setIsNewImageSelected(true);
        const reader = new FileReader();
        reader.onload = () => { setImgSrc(reader.result.toString() || ''); };
        reader.readAsDataURL(file);
    };

    const handleRemoveNewImage = () => {
        setImgSrc('');
        setCrop(undefined);
        setIsNewImageSelected(false);
        const fileInput = document.getElementById('edit-cover-upload');
        if (fileInput) fileInput.value = null;
    };

    const getCroppedImg = (image, crop) => {
        const canvas = document.createElement('canvas');
        const scaleX = image.naturalWidth / image.width;
        const scaleY = image.naturalHeight / image.height;
        canvas.width = crop.width;
        canvas.height = crop.height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(image, crop.x * scaleX, crop.y * scaleY, crop.width * scaleX, crop.height * scaleY, 0, 0, crop.width, crop.height);
        return new Promise((resolve, reject) => {
            canvas.toBlob(blob => {
                if (!blob) { reject(new Error('Canvas is empty')); return; }
                resolve(blob);
            }, 'image/jpeg', 0.95);
        });
    };

    const handleSubmit = async () => {
        const isTitleMissing = !title.trim();
        const isCoverMissing = !isAlbumSong && isNewImageSelected && (!imgSrc || !crop);

        if (isTitleMissing || isCoverMissing) {
            setShowValidation(true);
            setErrorMessage("Uzupełnij wymagane pola.");
            return;
        }

        setErrorMessage("");
        setIsLoading(true);

        try {
            const formData = new FormData();
            formData.append('title', title);

            if (!isAlbumSong) {
                formData.append('publiclyVisible', isPublic.toString());
                if (isNewImageSelected && imgRef.current && crop) {
                    const croppedImageBlob = await getCroppedImg(imgRef.current, crop);
                    formData.append('coverFile', croppedImageBlob, "cover.jpg");
                }
            }

            await updateSong(songToEdit.id, formData);

            if (onSongUpdated) {
                onSongUpdated();
            }
            onClose();

        } catch (error) {
            console.error("Błąd edycji:", error);
            setErrorMessage("Wystąpił błąd podczas aktualizacji utworu.");
        } finally {
            setIsLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content small-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>
                <h2 className="modal-title">Edytuj utwór</h2>

                <div className="edit-form edit-form-stack">
                    {isAlbumSong && (
                        <div className="album-warning">
                            <span>
                                <strong>Utwór należy do albumu.</strong><br />
                                Zmieniona zostanie tylko nazwa. Okładka i widoczność są dziedziczone z albumu.
                            </span>
                        </div>
                    )}

                    <div className="form-group">
                        <label>Tytuł utworu</label>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            className={showValidation && !title.trim() ? 'error-border' : ''}
                        />
                    </div>

                    {!isAlbumSong && (
                        <div className="form-group">
                            <label>Okładka</label>

                            {!isNewImageSelected && songToEdit?.coverArtUrl && (
                                <div className="cover-preview-container">
                                    <img
                                        src={songToEdit.coverArtUrl}
                                        alt="Obecna okładka"
                                        className="cover-preview-img"
                                    />
                                </div>
                            )}

                            {isNewImageSelected && imgSrc && (
                                <div className="cropper-container">
                                    <ReactCrop crop={crop} onChange={c => setCrop(c)} aspect={1}>
                                        <img ref={imgRef} src={imgSrc} alt="Crop" onLoad={onImageLoad} />
                                    </ReactCrop>
                                </div>
                            )}

                            <div className="cover-art-buttons">
                                <label htmlFor="edit-cover-upload" className="file-upload-button full-width-label">
                                    {isNewImageSelected ? 'Zmień wybrane' : 'Zmień okładkę'}
                                </label>
                                <input
                                    type="file"
                                    id="edit-cover-upload"
                                    className="hidden-input"
                                    accept="image/*"
                                    onChange={handleFileChange}
                                />
                                {isNewImageSelected && (
                                    <button
                                        type="button"
                                        onClick={handleRemoveNewImage}
                                        className="remove-image-button"
                                    >
                                        Przywróć
                                    </button>
                                )}
                            </div>
                        </div>
                    )}

                    {!isAlbumSong && (
                        <div className="form-group">
                            <fieldset className="visibility-selection centered-fieldset">
                                <legend>Widoczność</legend>
                                <div className="visibility-option">
                                    <input type="radio" id="edit-pub" checked={isPublic === true} onChange={() => setIsPublic(true)} />
                                    <label htmlFor="edit-pub">Publiczny</label>
                                </div>
                                <div className="visibility-option">
                                    <input type="radio" id="edit-priv" checked={isPublic === false} onChange={() => setIsPublic(false)} />
                                    <label htmlFor="edit-priv">Prywatny</label>
                                </div>
                            </fieldset>
                        </div>
                    )}

                    {errorMessage && <div className="validation-message">{errorMessage}</div>}

                    <div className="modal-actions">
                        <button className="cancel-btn" onClick={onClose}>Anuluj</button>
                        <button className="save-button no-margin-top" onClick={handleSubmit} disabled={isLoading}>
                            {isLoading ? 'Zapisywanie...' : 'Zapisz zmiany'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default EditSongModal;