import React, {useEffect, useRef, useState} from 'react';
import ReactCrop, { centerCrop, makeAspectCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import './AddSongModal.css';
import api from "../../context/axiosClient.js";

const genres = [
    "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
    "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
    "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
];

function AddSongModal({ isOpen, onClose }) {
    const [selectedGenres, setSelectedGenres] = useState([]);
    const [title, setTitle] = useState("");
    const [isPublic, setIsPublic] = useState(false);
    const [audioFile, setAudioFile] = useState(null);
    const [songFileName, setSongFileName] = useState('');
    const [imgSrc, setImgSrc] = useState('');
    const [crop, setCrop] = useState();
    const imgRef = useRef(null);

    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [showValidation, setShowValidation] = useState(false);

    function onImageLoad(e) {
        const { width, height } = e.currentTarget;
        const smallestDimension = Math.min(width, height);
        const cropSize = smallestDimension * 0.9;

        const crop = centerCrop(
            makeAspectCrop(
                {
                    unit: 'px',
                    width: cropSize,
                },
                1,
                width,
                height
            ),
            width,
            height
        );
        setCrop(crop);
    }

    const resetForm = () => {
        setTitle("");
        setSelectedGenres([]);
        setIsPublic(false);
        setAudioFile(null);
        setSongFileName('');
        setImgSrc('');
        setCrop(undefined);
        imgRef.current = null;
        setErrorMessage("");
        setShowValidation(false)

        const songInput = document.getElementById('song-upload');
        if (songInput) songInput.value = null;

        const coverInput = document.getElementById('cover-art-upload');
        if (coverInput) coverInput.value = null;
    };

    const handleCloseModal = () => {
        resetForm();
        onClose();
    };

    useEffect(() => {
        if (isOpen) {
            resetForm();
        }
    }, [isOpen]);

    const handleGenreClick = (genre) => {
        if (showValidation) setErrorMessage("");
        const isSelected = selectedGenres.includes(genre);
        if (isSelected) {
            setSelectedGenres(prev => prev.filter(g => g !== genre));
        } else {
            if (selectedGenres.length < 3) {
                setSelectedGenres(prev => [...prev, genre]);
            } else {
                setErrorMessage("Możesz wybrać maksymalnie 3 gatunki.");
            }
        }
    };

    const handleFileChange = (e, type) => {
        const file = e.target.files[0];
        if (!file) return;

        setErrorMessage("");

        if (type === 'song') {
            const fileName = file.name.toLowerCase();
            if (!fileName.endsWith('.m4a')) {
                alert("Niestety backend obsługuje tylko pliki .m4a");
                e.target.value = null;
                return;
            }

            setAudioFile(file);
            setSongFileName(file.name);
        } else if (type === 'cover') {
            setCrop(undefined);
            setImgSrc('');
            const reader = new FileReader();
            reader.onload = () => {
                setImgSrc(reader.result.toString() || '');
            };
            reader.readAsDataURL(file);
        }
    };

    const removeCoverArt = () => {
        setImgSrc('');
        setCrop(undefined);
        imgRef.current = null;
        const fileInput = document.getElementById('cover-art-upload').value = null;
        if (fileInput) {
            fileInput.value = null
        }
    };

    const getCroppedImg = (image, crop) => {
        const canvas = document.createElement('canvas');
        const scaleX = image.naturalWidth / image.width;
        const scaleY = image.naturalHeight / image.height;
        canvas.width = crop.width;
        canvas.height = crop.height;
        const ctx = canvas.getContext('2d');

        ctx.drawImage(
            image,
            crop.x * scaleX,
            crop.y * scaleY,
            crop.width * scaleX,
            crop.height * scaleY,
            0,
            0,
            crop.width,
            crop.height
        );

        return new Promise((resolve, reject) => {
            canvas.toBlob(blob => {
                if (!blob) {
                    reject(new Error('Canvas is empty'));
                    return;
                }
                resolve(blob);
            }, 'image/jpeg', 0.95);
        });
    };

    const handleSubmit = async () => {
        const isAudioMissing = !audioFile;
        const isCoverMissing = !imgSrc || !crop;
        const isTitleMissing = !title.trim();
        const isGenreMissing = selectedGenres.length === 0;

        if (isAudioMissing || isCoverMissing || isTitleMissing || isGenreMissing) {
            setShowValidation(true);
            if (isGenreMissing && !isAudioMissing && !isCoverMissing && !isTitleMissing) {
                setErrorMessage("Wybierz przynajmniej jeden gatunek muzyczny.");
            } else {
                setErrorMessage("Wypełnij wszystkie wymagane pola zaznaczone na czerwono.");
            }
            return;
        }

        setErrorMessage("");
        setShowValidation(false);
        setIsLoading(true);

        try {
            if (!imgRef.current) throw new Error("Błąd ładowania obrazka");

            const croppedImageBlob = await getCroppedImg(imgRef.current, crop);

            const formData = new FormData();

            formData.append('audioFile', audioFile);

            formData.append('coverFile', croppedImageBlob, "cover.jpg");

            formData.append('title', title);

            selectedGenres.forEach(g => formData.append('genre', g));

            formData.append('publiclyVisible', isPublic.toString());

            const response = await api.post("/songs/upload", formData, {
                headers: {
                    "Content-Type": "multipart/form-data",
                },
            })

            console.log("Server response:", response.data);
            handleCloseModal();

        } catch (error) {
            console.error("Błąd:", error);
            if (error.response && error.response.data) {
                alert("Błąd serwera: " + (error.response.data.message || "Sprawdź logi backendu"));
            } else {
                alert("Wystąpił błąd połączenia z serwerem.");
            }
        } finally {
            setIsLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={handleCloseModal}>×</button>
                <h2>Dodaj nowy utwór</h2>

                <div className="edit-form">
                    <div className="form-layout-container">

                        {/* 3. KOLUMNA LEWA (MEDIA) */}
                        <div className="form-column-left">
                            <label>Okładka utworu</label>
                            <div className="cover-art-buttons">
                                <label htmlFor="cover-art-upload" className={`file-upload-button ${showValidation && !imgSrc ? 'error-border' : ''}`} >
                                    {imgSrc ? 'Zmień okładkę' : 'Wybierz okładkę'}
                                </label>
                                <input
                                    type="file"
                                    id="cover-art-upload"
                                    className="file-upload-input"
                                    accept="image/png, image/jpeg, image/webp, image/avif"
                                    onChange={(e) => handleFileChange(e, 'cover')}
                                />
                                {imgSrc && (
                                    <button type="button" className="remove-image-button" onClick={removeCoverArt}>
                                        Usuń
                                    </button>
                                )}
                            </div>
                            {imgSrc && (
                                <div className="cropper-container">
                                    <ReactCrop
                                        crop={crop}
                                        onChange={c => setCrop(c)}
                                        aspect={1}
                                    >
                                        <img
                                            ref={imgRef}
                                            src={imgSrc}
                                            alt="Podgląd okładki"
                                            onLoad={onImageLoad}
                                        />
                                    </ReactCrop>
                                </div>
                            )}

                            <label htmlFor="song-upload">Plik utworu</label>
                            <label htmlFor="song-upload" className={`file-upload-button ${showValidation && !audioFile ? 'error-border' : ''}`}>
                                {songFileName || "Wybierz plik audio .m4a"}
                            </label>
                            <input
                                type="file"
                                id="song-upload"
                                className="file-upload-input"
                                accept=".m4a"
                                onChange={(e) => handleFileChange(e, 'song')}
                            />
                        </div>

                        {/* 4. KOLUMNA PRAWA (INFO) */}
                        <div className="form-column-right">
                            <label htmlFor="song-title">Tytuł utworu</label>
                            <input
                                type="text"
                                id="song-title"
                                placeholder="Tytuł"
                                value={title}
                                onChange={(e) => {
                                    setTitle(e.target.value);
                                    if(showValidation) setErrorMessage("");
                                }}
                                className={showValidation && !title.trim() ? 'error-border' : ''}
                            />

                            <label>Gatunek (Wybierz max 3)</label>
                            <div className={`genre-picker-container ${showValidation && selectedGenres.length === 0 ? 'error-border' : ''}`}>
                                {genres.map(genre => (
                                    <div
                                        key={genre}
                                        className={`genre-pill ${selectedGenres.includes(genre) ? 'selected' : ''}`}
                                        onClick={() => handleGenreClick(genre)}
                                    >
                                        {genre}
                                    </div>
                                ))}
                            </div>

                            <fieldset className="visibility-selection">
                                <legend>Widoczność</legend>
                                <div className="visibility-option">
                                    <input
                                        type="radio"
                                        id="public"
                                        name="visibility"
                                        checked={isPublic === true}
                                        onChange={() => setIsPublic(true)} />
                                    <label htmlFor="public">Publiczny</label>
                                </div>
                                <div className="visibility-option">
                                    <input
                                        type="radio"
                                        id="private"
                                        name="visibility"
                                        checked={isPublic === false}
                                        onChange={() => setIsPublic(false)}
                                    />
                                    <label htmlFor="private">Prywatny</label>
                                </div>
                            </fieldset>
                        </div>
                    </div>

                    {errorMessage && (
                        <div className="validation-message">
                            {errorMessage}
                        </div>
                    )}

                    {/* 5. PRZYCISK ZAPISU */}
                    <button
                        className="save-button"
                        onClick={handleSubmit}
                        disabled={isLoading}
                    >
                        {isLoading ? 'Wysyłanie...' : 'Prześlij utwór'}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default AddSongModal;