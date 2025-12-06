import React, {useEffect, useRef, useState} from 'react';
import ReactCrop, { centerCrop, makeAspectCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import '../song/AddSongModal.css';
import { updatePlaylist } from '../../services/playlistService.js';
import { getImageUrl } from '../../services/imageService.js'; // Potrzebne do wyświetlenia starej okładki

function EditPlaylistModal({ isOpen, onClose, playlistToEdit, onPlaylistUpdated, playlistSongs = [] }) {
    const [title, setTitle] = useState("");
    const [isPublic, setIsPublic] = useState(false);

    // Obrazek
    const [imgSrc, setImgSrc] = useState('');
    const [crop, setCrop] = useState();
    const [isNewImageSelected, setIsNewImageSelected] = useState(false);
    const imgRef = useRef(null);

    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [showValidation, setShowValidation] = useState(false);

    // --- PRE-FILLING DANYCH ---
    useEffect(() => {
        if (isOpen && playlistToEdit) {
            setTitle(playlistToEdit.title || playlistToEdit.name || "");
            const pubVisible = playlistToEdit.publiclyVisible === true || playlistToEdit.publiclyVisible === "true";
            setIsPublic(pubVisible);

            // Reset obrazka
            setImgSrc('');
            setIsNewImageSelected(false);
            setErrorMessage("");
            setShowValidation(false);
        }
    }, [isOpen, playlistToEdit]);

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
        const fileInput = document.getElementById('edit-pl-cover-upload');
        if(fileInput) fileInput.value = null;
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
        const isCoverMissing = isNewImageSelected && (!imgSrc || !crop);

        if (isTitleMissing || isCoverMissing) {
            setShowValidation(true);
            setErrorMessage("Uzupełnij wymagane pola.");
            return;
        }

        // --- WALIDACJA PRYWATNOŚCI ---
        // Jeśli próbujemy zmienić na PUBLICZNĄ, sprawdzamy zawartość
        if (isPublic) {
            // Szukamy PIERWSZEJ piosenki, która jest prywatna
            const privateSong = playlistSongs.find(song => {
                // Sprawdzamy czy flaga jest jawnie false
                return song.publiclyVisible === false || song.publiclyVisible === "false";
            });

            if (privateSong) {
                // Jeśli znaleziono, blokujemy zapis i wyświetlamy komunikat
                // Sprawdzamy czy to przez album (jeśli mamy albumId)
                const reason = privateSong.albumId
                    ? `jest częścią prywatnego albumu`
                    : `jest oznaczony jako prywatny`;

                setErrorMessage(`Nie można zmienić playlisty na publiczną. Utwór "${privateSong.title}" ${reason}.`);
                return; // STOP
            }
        }

        setErrorMessage("");
        setIsLoading(true);

        try {
            const formData = new FormData();
            formData.append('title', title);
            formData.append('publiclyVisible', isPublic.toString());

            if (isNewImageSelected && imgRef.current && crop) {
                const croppedImageBlob = await getCroppedImg(imgRef.current, crop);
                formData.append('coverFile', croppedImageBlob, "cover.jpg");
            }

            await updatePlaylist(playlistToEdit.id, formData);

            if (onPlaylistUpdated) {
                onPlaylistUpdated();
            }
            onClose();

        } catch (error) {
            console.error("Błąd edycji playlisty:", error);
            setErrorMessage("Wystąpił błąd podczas aktualizacji playlisty.");
        } finally {
            setIsLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()} style={{maxWidth: '450px'}}>
                <button className="close-button" onClick={onClose}>×</button>
                <h2 style={{textAlign: 'center', marginBottom: '1.5rem'}}>Edytuj playlistę</h2>

                <div className="edit-form" style={{display: 'flex', flexDirection: 'column', gap: '1.2rem'}}>

                    {/* 1. TYTUŁ */}
                    <div style={{display: 'flex', flexDirection: 'column', gap: '5px'}}>
                        <label>Nazwa playlisty</label>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            className={showValidation && !title.trim() ? 'error-border' : ''}
                        />
                    </div>

                    {/* 2. OKŁADKA */}
                    <div style={{display: 'flex', flexDirection: 'column', gap: '10px'}}>
                        <label>Okładka</label>

                        {/* WIDOK: STARA OKŁADKA (Jeśli nie wybrano nowej) */}
                        {!isNewImageSelected && playlistToEdit?.coverStorageKeyId && (
                            <div style={{display: 'flex', justifyContent: 'center', marginBottom: '5px'}}>
                                <img
                                    src={getImageUrl(playlistToEdit.coverStorageKeyId)}
                                    alt="Obecna okładka"
                                    style={{width: '150px', height: '150px', borderRadius: '8px', objectFit: 'cover', border: '1px solid #333'}}
                                />
                            </div>
                        )}

                        {/* WIDOK: NOWA OKŁADKA (Cropper) */}
                        {isNewImageSelected && imgSrc && (
                            <div className="cropper-container">
                                <ReactCrop crop={crop} onChange={c => setCrop(c)} aspect={1}>
                                    <img ref={imgRef} src={imgSrc} alt="Crop" onLoad={onImageLoad} />
                                </ReactCrop>
                            </div>
                        )}

                        <div className="cover-art-buttons" style={{display: 'flex', gap: '10px'}}>
                            <label htmlFor="edit-pl-cover-upload" className={`file-upload-button`} style={{flex: 1, textAlign: 'center', cursor: 'pointer'}}>
                                {isNewImageSelected ? 'Zmień wybrane' : 'Zmień okładkę'}
                            </label>
                            <input
                                type="file"
                                id="edit-pl-cover-upload"
                                className="file-upload-input"
                                accept="image/*"
                                onChange={handleFileChange}
                                style={{display: 'none'}}
                            />
                            {isNewImageSelected && (
                                <button
                                    type="button"
                                    onClick={handleRemoveNewImage}
                                    className="remove-image-button"
                                    style={{padding: '10px 15px'}}
                                >
                                    Przywróć
                                </button>
                            )}
                        </div>
                    </div>

                    {/* 3. WIDOCZNOŚĆ */}
                    <div style={{display: 'flex', flexDirection: 'column', gap: '5px'}}>
                        <fieldset className="visibility-selection" style={{justifyContent: 'center', marginTop: '5px'}}>
                            <legend style={{textAlign: 'center'}}>Widoczność</legend>
                            <div className="visibility-option">
                                <input type="radio" id="edit-pl-pub" checked={isPublic === true} onChange={() => setIsPublic(true)} />
                                <label htmlFor="edit-pl-pub">Publiczna</label>
                            </div>
                            <div className="visibility-option">
                                <input type="radio" id="edit-pl-priv" checked={isPublic === false} onChange={() => setIsPublic(false)} />
                                <label htmlFor="edit-pl-priv">Prywatna</label>
                            </div>
                        </fieldset>
                    </div>

                    {errorMessage && <div className="validation-message">{errorMessage}</div>}

                    <div style={{display:'flex', gap:'10px', marginTop:'1rem'}}>
                        <button className="cancel-btn" onClick={onClose} style={{flex:1}}>Anuluj</button>
                        <button className="save-button" onClick={handleSubmit} disabled={isLoading} style={{flex:1, marginTop:0}}>
                            {isLoading ? 'Zapisywanie...' : 'Zapisz zmiany'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default EditPlaylistModal;