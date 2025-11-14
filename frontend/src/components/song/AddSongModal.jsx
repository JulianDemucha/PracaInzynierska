// Plik: src/components/song/AddSongModal.jsx
import React, { useState } from 'react';
import ReactCrop, { centerCrop, makeAspectCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import './AddSongModal.css';

const genres = [
    "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
    "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
    "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
];

function AddSongModal({ isOpen, onClose }) {
    // ... (wszystkie twoje stany i funkcje zostają bez zmian) ...
    const [selectedGenres, setSelectedGenres] = useState([]);
    const [songFileName, setSongFileName] = useState('');
    const [imgSrc, setImgSrc] = useState('');
    const [crop, setCrop] = useState();

    const handleGenreClick = (genre) => { /* ... (bez zmian) ... */
        const isSelected = selectedGenres.includes(genre);
        if (isSelected) {
            setSelectedGenres(prev => prev.filter(g => g !== genre));
        } else {
            if (selectedGenres.length < 3) {
                setSelectedGenres(prev => [...prev, genre]);
            } else {
                alert("Możesz wybrać maksymalnie 3 gatunki.");
            }
        }
    };
    const handleFileChange = (e, type) => { /* ... (bez zmian) ... */
        const file = e.target.files[0];
        if (!file) return;
        if (type === 'song') {
            setSongFileName(file.name);
        } else if (type === 'cover') {
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
        }
    };
    const removeCoverArt = () => { /* ... (bez zmian) ... */
        setImgSrc('');
        document.getElementById('cover-art-upload').value = null;
    };

    if (!isOpen) return null;

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>
                <h2>Dodaj nowy utwór</h2>

                {/* 1. GŁÓWNY KONTENER FORMULARZA */}
                <div className="edit-form">

                    {/* 2. NOWY KONTENER DLA DWÓCH KOLUMN */}
                    <div className="form-layout-container">

                        {/* 3. KOLUMNA LEWA (MEDIA) */}
                        <div className="form-column-left">
                            <label>Okładka utworu</label>
                            <div className="cover-art-buttons">
                                <label htmlFor="cover-art-upload" className="file-upload-button">
                                    {imgSrc ? 'Zmień okładkę' : 'Wybierz okładkę (.png, .jpg)'}
                                </label>
                                <input
                                    type="file"
                                    id="cover-art-upload"
                                    className="file-upload-input"
                                    accept="image/png, image/jpeg"
                                    onChange={(e) => handleFileChange(e, 'cover')}
                                />
                                {imgSrc && (
                                    <button
                                        type="button"
                                        className="remove-image-button"
                                        onClick={removeCoverArt}
                                    >Usuń</button>
                                )}
                            </div>
                            {imgSrc && (
                                <div className="cropper-container">
                                    <ReactCrop
                                        crop={crop}
                                        onChange={c => setCrop(c)}
                                        aspect={1}
                                    >
                                        <img src={imgSrc} alt="Podgląd okładki"/>
                                    </ReactCrop>
                                </div>
                            )}

                            <label htmlFor="song-upload">Plik utworu</label>
                            <label htmlFor="song-upload" className="file-upload-button">
                                {songFileName || "Wybierz plik audio (.mp3, .wav)"}
                            </label>
                            <input
                                type="file"
                                id="song-upload"
                                className="file-upload-input"
                                accept="audio/mpeg, audio/wav"
                                onChange={(e) => handleFileChange(e, 'song')}
                            />
                        </div>

                        {/* 4. KOLUMNA PRAWA (INFO) */}
                        <div className="form-column-right">
                            <label htmlFor="song-title">Tytuł utworu</label>
                            <input type="text" id="song-title" placeholder="Nazwa utworu" />

                            <label>Gatunek (Wybierz max 3)</label>
                            <div className="genre-picker-container">
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
                                    <input type="radio" id="public" name="visibility" value="PUBLIC" defaultChecked />
                                    <label htmlFor="public">Publiczny</label>
                                </div>
                                <div className="visibility-option">
                                    <input type="radio" id="private" name="visibility" value="PRIVATE" />
                                    <label htmlFor="private">Prywatny</label>
                                </div>
                            </fieldset>
                        </div>
                    </div>

                    {/* 5. PRZYCISK ZAPISU (teraz na dole, poza kolumnami) */}
                    <button className="save-button">Prześlij utwór</button>
                </div>
            </div>
        </div>
    );
}

export default AddSongModal;