import React, { useState, useEffect, useRef } from 'react';
import ReactCrop, { centerCrop, makeAspectCrop } from "react-image-crop";
import 'react-image-crop/dist/ReactCrop.css';

import { getUserPlaylists, createPlaylist, addSongToPlaylist } from '../../services/playlistService.js';
import { useAuth } from '../../context/useAuth.js';
import './AddToPlaylistModal.css';

function AddToPlaylistModal({ isOpen, onClose, songToAdd }) {
    const { currentUser } = useAuth();

    const [playlists, setPlaylists] = useState([]);
    const [loading, setLoading] = useState(false);
    const [view, setView] = useState('list');
    const [errorMessage, setErrorMessage] = useState("");
    const [showValidation, setShowValidation] = useState(false);

    const [newPlaylistName, setNewPlaylistName] = useState("");
    const [isPublic, setIsPublic] = useState(false);

    const [imgSrc, setImgSrc] = useState('');
    const [crop, setCrop] = useState();
    const imgRef = useRef(null);

    useEffect(() => {
        if (isOpen) {
            if (currentUser) fetchPlaylists();
            resetCreateForm();
        }
    }, [isOpen, currentUser]);

    function onImageLoad(e) {
        const {width, height} = e.currentTarget;
        if (width === 0 || height === 0) return;

        const smallestDimension = Math.min(width, height);
        const cropSize = smallestDimension * 0.9;

        const crop = centerCrop(
            makeAspectCrop({ unit: 'px', width: cropSize }, 1, width, height),
            width,
            height
        );
        setCrop(crop);
    }

    const resetCreateForm = () => {
        setView('list');
        setNewPlaylistName("");
        setErrorMessage("");
        setShowValidation(false);
        setIsPublic(false);
        setImgSrc('');
        setCrop(undefined);
        imgRef.current = null;
        const fileInput = document.getElementById('playlist-cover-upload');
        if (fileInput) fileInput.value = null;
    };

    const fetchPlaylists = async () => {
        setLoading(true);
        try {
            const data = await getUserPlaylists(currentUser.id);
            setPlaylists(data || []);
        } catch (error) {
            console.error("Błąd pobierania playlist:", error);
            setPlaylists([]);
        } finally {
            setLoading(false);
        }
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        setErrorMessage("");
        setCrop(undefined);
        setImgSrc('');
        const reader = new FileReader();
        reader.onload = () => { setImgSrc(reader.result.toString() || ''); };
        reader.readAsDataURL(file);
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
                if (!blob) reject(new Error('Canvas is empty'));
                else resolve(blob);
            }, 'image/jpeg', 0.95);
        });
    };

    const handleAddToExisting = async (targetPlaylist) => {
        if (!songToAdd) return;

        const isSongPrivate = songToAdd.publiclyVisible === false || songToAdd.publiclyVisible === "false";
        const isPlaylistPublic = targetPlaylist.publiclyVisible === true || targetPlaylist.publiclyVisible === "true";

        if (isSongPrivate && isPlaylistPublic) {
            alert(`Nie można dodać prywatnego utworu "${songToAdd.title}" do publicznej playlisty "${targetPlaylist.title}"!`);
            return;
        }

        setLoading(true);
        try {
            await addSongToPlaylist(targetPlaylist.id, songToAdd.id);
            alert(`Dodano "${songToAdd.title}" do playlisty "${targetPlaylist.title || targetPlaylist.name}"`);
            onClose();
        } catch (error) {
            console.error("Błąd dodawania:", error);
            const status = error.response?.status;
            const message = error.response?.data?.message || "";

            if (status === 409 || status === 400 || message.includes("duplicate") || message.includes("exists")) {
                alert(`Ten utwór znajduje się już na tej playliście!`);
            } else {
                alert("Nie udało się dodać utworu do playlisty.");
            }
        } finally {
            setLoading(false);
        }
    };

    const handleCreateAndAdd = async () => {
        const isNameMissing = !newPlaylistName.trim();
        const isCoverMissing = !imgSrc || !crop;

        if (isNameMissing || isCoverMissing) {
            setShowValidation(true);
            setErrorMessage("Nazwa playlisty i okładka są wymagane!");
            return;
        }

        if (songToAdd) {
            const isSongPrivate = songToAdd.publiclyVisible === false || songToAdd.publiclyVisible === "false";

            if (isSongPrivate && isPublic) {
                setErrorMessage("Nie możesz stworzyć publicznej playlisty zawierającej prywatny utwór.");
                return;
            }
        }

        setLoading(true);
        try {
            const coverBlob = await getCroppedImg(imgRef.current, crop);
            const formData = new FormData();
            formData.append('title', newPlaylistName);
            formData.append('publiclyVisible', isPublic.toString());
            formData.append('coverFile', coverBlob, "cover.jpg");

            const newPlaylist = await createPlaylist(formData);
            const createdName = newPlaylist.title || newPlaylist.name || newPlaylistName;

            if (songToAdd && newPlaylist?.id) {
                await addSongToPlaylist(newPlaylist.id, songToAdd.id);
                alert(`Utworzono playlistę "${createdName}" i dodano utwór!`);
            } else {
                alert(`Utworzono playlistę "${createdName}"!`);
            }
            onClose();
        } catch (error) {
            console.error("Błąd:", error);
            if (error.response && error.response.data) {
                setErrorMessage("Błąd: " + (error.response.data.message || "Błąd serwera"));
            } else {
                setErrorMessage("Nie udało się utworzyć playlisty.");
            }
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="playlist-modal-backdrop" onClick={onClose}>
            <div className={`playlist-modal-content ${view === 'create' ? 'expanded' : ''}`} onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>

                <h2>Dodaj do playlisty</h2>
                {songToAdd && view === 'list' && (
                    <div className="song-preview-text">
                        Wybrany utwór: <strong>{songToAdd.title}</strong>
                        <div style={{fontSize:'0.8rem', color:'#888', marginTop:'4px'}}>
                            Status: {songToAdd.publiclyVisible === false ? "Prywatny" : "Publiczny"}
                        </div>
                    </div>
                )}

                {view === 'list' && (
                    <div className="playlist-list-container">
                        <div className="playlists-scroll-area">
                            {loading && playlists.length === 0 ? (
                                <p style={{color:'#666', textAlign:'center'}}>Ładowanie...</p>
                            ) : (
                                playlists.length > 0 ? (
                                    playlists.map(pl => {
                                        const isSongPrivate = songToAdd?.publiclyVisible === false;
                                        const isPlPublic = pl.publiclyVisible === true;
                                        const isBlocked = isSongPrivate && isPlPublic;

                                        return (
                                            <div
                                                key={pl.id}
                                                className="playlist-row"
                                                onClick={() => !isBlocked && handleAddToExisting(pl)}
                                                style={{
                                                    opacity: isBlocked ? 0.5 : 1,
                                                    cursor: isBlocked ? 'not-allowed' : 'pointer'
                                                }}
                                            >
                                                <div className="playlist-cover-placeholder">♪</div>

                                                <div className="playlist-info-row">
                                                    <span className="playlist-name">{pl.title || pl.name || "Bez tytułu"}</span>

                                                    <div style={{display:'flex', flexDirection:'column', alignItems:'flex-end'}}>
                                                        <span className="playlist-count-badge">
                                                            {pl.songsCount} {pl.songsCount === 1 ? 'utwór' : 'utworów'}
                                                        </span>
                                                        {/* Pokazujemy status playlisty */}
                                                        <span style={{fontSize:'0.7rem', color:'#666', marginTop:'2px'}}>
                                                            {pl.publiclyVisible ? 'Publiczna' : 'Prywatna'}
                                                        </span>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    })
                                ) : (
                                    <p style={{color:'#666', textAlign:'center', padding: '10px'}}>Brak playlist</p>
                                )
                            )}
                        </div>

                        <button className="create-new-row" onClick={() => setView('create')}>
                            <div className="plus-box">+</div>
                            <span>Nowa playlista</span>
                        </button>
                    </div>
                )}

                {view === 'create' && (
                    <div className="create-playlist-form">
                        <div className="form-group">
                            <label>Nazwa playlisty</label>
                            <input
                                type="text"
                                value={newPlaylistName}
                                onChange={(e) => { setNewPlaylistName(e.target.value); if(showValidation) setErrorMessage(""); }}
                                placeholder="Np. Hity lata"
                                className={showValidation && !newPlaylistName.trim() ? 'error-border' : ''}
                                autoFocus
                            />
                        </div>

                        <div className="form-group">
                            <label>Okładka (Wymagana)</label>
                            <label htmlFor="playlist-cover-upload" className={`file-upload-button-small ${showValidation && !imgSrc ? 'error-border' : ''}`}>
                                {imgSrc ? 'Zmień zdjęcie' : 'Wybierz zdjęcie'}
                            </label>
                            <input id="playlist-cover-upload" type="file" accept="image/png, image/jpeg, image/webp" onChange={handleFileChange} style={{display: 'none'}} />
                            {imgSrc && (
                                <div className="playlist-cropper-container">
                                    <ReactCrop
                                        crop={crop}
                                        onChange={(_, percentCrop) => setCrop(percentCrop)}
                                        onComplete={(c) => setCrop(c)}
                                        aspect={1}
                                    >
                                        <img
                                            ref={imgRef}
                                            src={imgSrc}
                                            alt="Cover"
                                            onLoad={onImageLoad}
                                        />
                                    </ReactCrop>
                                </div>
                            )}
                        </div>

                        <div className="form-group visibility-group">
                            <label>Widoczność:</label>
                            <div className="radio-options">
                                <label className={`radio-label ${!isPublic ? 'selected' : ''}`}>
                                    <input type="radio" name="visibility" checked={!isPublic} onChange={() => setIsPublic(false)} /> Prywatna
                                </label>
                                <label className={`radio-label ${isPublic ? 'selected' : ''}`}>
                                    <input type="radio" name="visibility" checked={isPublic} onChange={() => setIsPublic(true)} /> Publiczna
                                </label>
                            </div>
                        </div>

                        {errorMessage && <div className="validation-message">{errorMessage}</div>}

                        <div className="form-actions">
                            <button className="cancel-btn" onClick={() => setView('list')}>Wróć</button>
                            <button className="save-button" onClick={handleCreateAndAdd} disabled={loading}>{loading ? 'Tworzenie...' : 'Stwórz i dodaj'}</button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default AddToPlaylistModal;