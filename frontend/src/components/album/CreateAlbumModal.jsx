import React, {useState, useRef, useEffect} from 'react';
import ReactCrop, {centerCrop, makeAspectCrop} from "react-image-crop";
import api from "../../context/axiosClient.js";
import { createAlbum } from '../../services/albumService.js';
import { useAuth } from '../../context/useAuth.js';
import 'react-image-crop/dist/ReactCrop.css';
import './CreateAlbumModal.css';
import defaultAvatar from '../../assets/images/default-avatar.png';

const genres = [
    "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
    "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
    "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
];

// DODANO: prop existingAlbumId oraz onAlbumUpdate
function CreateAlbumModal({isOpen, onClose, existingAlbumId = null, onAlbumUpdate}) {
    const { currentUser } = useAuth();

    const [step, setStep] = useState(1);
    const [isLoading, setIsLoading] = useState(false);

    // --- WALIDACJA ---
    const [showValidation, setShowValidation] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    // Dane Albumu
    const [albumTitle, setAlbumTitle] = useState("");
    const [selectedGenres, setSelectedGenres] = useState([]);
    const [isPublic, setIsPublic] = useState(false);

    // Obrazek
    const [imgSrc, setImgSrc] = useState(defaultAvatar);
    const [crop, setCrop] = useState();
    const imgRef = useRef(null);
    const [albumCoverBlob, setAlbumCoverBlob] = useState(null);

    // Wynik kroku 1
    const [createdAlbumId, setCreatedAlbumId] = useState(null);

    // Dane Piosenki
    const [songTitle, setSongTitle] = useState("");
    const [audioFile, setAudioFile] = useState(null);
    const [addedSongsCount, setAddedSongsCount] = useState(0);

    // Resetowanie i ustawianie trybu
    useEffect(() => {
        if (isOpen) {
            // Zmiana: Jeśli mamy existingAlbumId, wchodzimy od razu w krok 2
            if (existingAlbumId) {
                setStep(2);
                setCreatedAlbumId(existingAlbumId);
                // Resetujemy pola piosenki, ale nie albumu (bo album już jest)
                setSongTitle("");
                setAudioFile(null);
                setAddedSongsCount(0);
                setShowValidation(false);
                setErrorMessage("");
            } else {
                // Tryb tworzenia nowego albumu
                setStep(1);
                setAlbumTitle("");
                setSelectedGenres([]);
                setIsPublic(false);
                setImgSrc(defaultAvatar);
                setCrop(undefined);
                setAlbumCoverBlob(null);
                setCreatedAlbumId(null);
                setSongTitle("");
                setAudioFile(null);
                setAddedSongsCount(0);
                setShowValidation(false);
                setErrorMessage("");
            }
        }
    }, [isOpen, existingAlbumId]);

    function onImageLoad(e) {
        const { width, height } = e.currentTarget;
        if (width === 0 || height === 0) return;

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
    if (!isOpen) return null;

    // --- PLIKI ---
    const handleFileChange = (e, type) => {
        const file = e.target.files[0];
        if (!file) return;
        setErrorMessage("");

        if (type === 'cover') {
            setCrop(undefined);
            setImgSrc('');
            const reader = new FileReader();
            reader.onload = () => {
                setImgSrc(reader.result.toString() || '');
            };
            reader.readAsDataURL(file);
        } else if (type === 'song') {
            if (!file.name.toLowerCase().endsWith('.m4a')) {
                setErrorMessage("Wymagany plik .m4a!");
                return;
            }
            setAudioFile(file);
        }
    };

    const getCroppedImg = (image, crop) => {
        const canvas = document.createElement('canvas');
        const scaleX = image.naturalWidth / image.width;
        const scaleY = image.naturalHeight / image.height;
        canvas.width = crop.width;
        canvas.height = crop.height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(image, crop.x * scaleX, crop.y * scaleY, crop.width * scaleX, crop.height * scaleY, 0, 0, crop.width, crop.height);
        return new Promise((resolve) => canvas.toBlob(blob => resolve(blob), 'image/jpeg', 0.95));
    };

    const handleGenreClick = (genre) => {
        if (showValidation) setErrorMessage("");
        const isSelected = selectedGenres.includes(genre);
        if (isSelected) setSelectedGenres(prev => prev.filter(g => g !== genre));
        else if (selectedGenres.length < 3) setSelectedGenres(prev => [...prev, genre]);
        else alert("Max 3 gatunki");
    };

    // --- KROK 1: TWORZENIE ALBUMU ---
    const handleCreateAlbum = async () => {
        const isTitleMissing = !albumTitle.trim();
        const isCoverMissing = imgSrc === defaultAvatar || !crop;
        const isGenreMissing = selectedGenres.length === 0;

        if (isTitleMissing || isCoverMissing || isGenreMissing) {
            setShowValidation(true);
            setErrorMessage("Uzupełnij wymagane pola (tytuł, okładka, gatunek).");
            return;
        }

        if (!currentUser) {
            alert("Błąd: Brak zalogowanego użytkownika.");
            return;
        }

        setErrorMessage("");
        setShowValidation(false);
        setIsLoading(true);

        try {
            const blob = await getCroppedImg(imgRef.current, crop);
            const formData = new FormData();
            formData.append('coverFile', blob, "cover.jpg");
            formData.append('title', albumTitle);
            formData.append('description', albumTitle);
            selectedGenres.forEach(g => formData.append('genre', g));
            formData.append('publiclyVisible', isPublic.toString());
            setAlbumCoverBlob(blob);

            const albumDto = await createAlbum(formData);
            setCreatedAlbumId(albumDto.id);
            setStep(2);
        } catch (error) {
            console.error("Błąd tworzenia albumu:", error);
            setErrorMessage("Błąd serwera: Nie udało się utworzyć albumu.");
        } finally {
            setIsLoading(false);
        }
    };

    // --- KROK 2: DODAWANIE PIOSENKI ---
    const handleAddSong = async () => {
        const isSongTitleMissing = !songTitle.trim();
        const isAudioMissing = !audioFile;

        if (isSongTitleMissing || isAudioMissing) {
            setShowValidation(true);
            setErrorMessage("Wpisz tytuł i wybierz plik audio.");
            return;
        }

        setErrorMessage("");
        setShowValidation(false);
        setIsLoading(true);

        try {
            const formData = new FormData();
            formData.append('audioFile', audioFile);
            formData.append('title', songTitle);

            // Jeśli to tryb edycji (existingAlbumId), nie wysyłamy okładki,
            // chyba że backend tego bezwzględnie wymaga.
            // Zazwyczaj przy dodawaniu do istniejącego albumu coverFile jest opcjonalny lub ignorowany.
            // Jeśli backend wymaga, musielibyśmy pobrać cover z URL, co jest trudne.
            // Zakładam, że w trybie edycji okładka nie jest potrzebna, bo album ją ma.
            if (!existingAlbumId && albumCoverBlob) {
                formData.append('coverFile', albumCoverBlob, "cover.jpg");
            }

            // W trybie edycji gatunki i publiczność są brane z albumu,
            // ale jeśli endpoint wymaga, wysyłamy puste lub domyślne.
            if (!existingAlbumId) {
                selectedGenres.forEach(g => formData.append('genre', g));
                formData.append('publiclyVisible', isPublic.toString());
            }

            // Ważne: endpoint POST /api/albums/{id}/add
            // W albumService użyjemy tego ID

            // Tutaj logika się zmienia zależnie od tego czy używamy endpointu `createAlbum` (tam piosenki nie szły)
            // czy `addSongToAlbum`. W poprzednim kodzie używałeś:
            // await api.post(`albums/${createdAlbumId}/add`, ...)

            await api.post(`albums/${createdAlbumId}/add`, formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });

            setSongTitle("");
            setAudioFile(null);
            const fileInput = document.getElementById('song-upload-step2');
            if(fileInput) fileInput.value = null;

            setAddedSongsCount(prev => prev + 1);

        } catch (error) {
            console.error("Błąd piosenki:", error);
            setErrorMessage("Nie udało się dodać piosenki. Spróbuj ponownie.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleFinish = () => {
        // Jeśli jesteśmy w trybie edycji, pozwalamy wyjść nawet jak dodano 0 (bo może użytkownik się rozmyślił)
        // Jeśli tworzymy nowy album, wymuszamy 1 piosenkę.
        if (!existingAlbumId && addedSongsCount === 0) {
            setShowValidation(true);
            setErrorMessage("Album musi zawierać przynajmniej jedną piosenkę!");
            return;
        }

        // Wywołaj callback odświeżania danych w rodzicu
        if (onAlbumUpdate) onAlbumUpdate();

        onClose();
    };

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>

                {step === 1 ? (
                    /* --- KROK 1: DANE ALBUMU (Tylko przy tworzeniu nowego) --- */
                    <>
                        <h2>Stwórz Album (1/2)</h2>
                        <div className="edit-form">
                            <label>Nazwa albumu</label>
                            <input
                                type="text" placeholder="Tytuł"
                                value={albumTitle}
                                onChange={e => {
                                    setAlbumTitle(e.target.value);
                                    if(showValidation) setErrorMessage("");
                                }}
                                className={showValidation && !albumTitle.trim() ? 'error-border' : ''}
                            />

                            <label>Okładka</label>
                            <div className="cover-art-buttons">
                                <label
                                    htmlFor="album-cover"
                                    className={`file-upload-button ${showValidation && imgSrc === defaultAvatar ? 'error-border' : ''}`}
                                >
                                    {imgSrc === defaultAvatar ? 'Wybierz okładkę' : 'Zmień'}
                                </label>
                                <input
                                    type="file" id="album-cover" className="file-upload-input"
                                    accept="image/*" onChange={(e) => handleFileChange(e, 'cover')}
                                />
                            </div>

                            {imgSrc !== defaultAvatar && (
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

                            <label>Gatunek</label>
                            <div className={`genre-picker-container ${showValidation && selectedGenres.length === 0 ? 'error-border' : ''}`}>
                                {genres.map(g => (
                                    <div key={g}
                                         className={`genre-pill ${selectedGenres.includes(g) ? 'selected' : ''}`}
                                         onClick={() => handleGenreClick(g)}
                                    >{g}</div>
                                ))}
                            </div>

                            <fieldset className="visibility-selection">
                                <legend>Widoczność</legend>
                                <div className="visibility-option">
                                    <input type="radio" id="pub" checked={isPublic} onChange={() => setIsPublic(true)} />
                                    <label htmlFor="pub">Publiczny</label>
                                </div>
                                <div className="visibility-option">
                                    <input type="radio" id="priv" checked={!isPublic} onChange={() => setIsPublic(false)} />
                                    <label htmlFor="priv">Prywatny</label>
                                </div>
                            </fieldset>

                            {errorMessage && <div className="validation-message">{errorMessage}</div>}

                            <button className="save-button" onClick={handleCreateAlbum} disabled={isLoading}>
                                {isLoading ? 'Tworzenie...' : 'Dalej: Dodaj utwory'}
                            </button>
                        </div>
                    </>
                ) : (
                    /* --- KROK 2: PIOSENKI (Tworzenie i Edycja) --- */
                    <>
                        <h2>{existingAlbumId ? 'Dodaj utwory do albumu' : 'Dodaj utwory do albumu'}</h2>
                        <div className="edit-form">
                            {!existingAlbumId && (
                                <div className="info-text">
                                    Album utworzony! Teraz dodaj do niego piosenki.<br/>
                                </div>
                            )}
                            {existingAlbumId && (
                                <div className="info-text">
                                    Dodajesz nowe utwory do istniejącego albumu.
                                </div>
                            )}

                            <label>Tytuł utworu</label>
                            <input
                                type="text" placeholder="Tytuł"
                                value={songTitle}
                                onChange={e => {
                                    setSongTitle(e.target.value);
                                    if(showValidation) setErrorMessage("");
                                }}
                                className={showValidation && !songTitle.trim() ? 'error-border' : ''}
                            />

                            <label>Plik audio (.m4a)</label>
                            <label
                                htmlFor="song-upload-step2"
                                className={`file-upload-button ${showValidation && !audioFile ? 'error-border' : ''}`}
                            >
                                {audioFile ? audioFile.name : "Wybierz plik"}
                            </label>
                            <input
                                type="file" id="song-upload-step2" className="file-upload-input"
                                accept=".m4a" onChange={(e) => handleFileChange(e, 'song')}
                            />

                            {errorMessage && <div className="validation-message">{errorMessage}</div>}

                            <div style={{display:'flex', gap:'10px', marginTop:'20px'}}>
                                <button className="add-song-button-small" onClick={handleAddSong} disabled={isLoading}>
                                    {isLoading ? 'Dodawanie...' : '+ Dodaj utwór'}
                                </button>
                                <button className="finish-button" onClick={handleFinish} disabled={isLoading}>
                                    {existingAlbumId ? 'Zakończ' : `Zakończ (${addedSongsCount})`}
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}

export default CreateAlbumModal;