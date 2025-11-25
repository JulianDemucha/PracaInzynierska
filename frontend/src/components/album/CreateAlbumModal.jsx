import React, {useState, useRef, useEffect} from 'react';
import ReactCrop, {centerCrop, makeAspectCrop} from "react-image-crop";
import api from "../../context/axiosClient.js";
import { createAlbum, addSongToAlbum } from '../../services/albumService.js';
import { useAuth } from '../../context/useAuth.js'; // POTRZEBNE DO AUTHOR_ID
import 'react-image-crop/dist/ReactCrop.css';
import './CreateAlbumModal.css';
import defaultAvatar from '../../assets/images/default-avatar.png';

const genres = [
    "POP", "ROCK", "JAZZ", "BLUES", "HIP_HOP", "RNB", "ELECTRONIC",
    "DANCE", "METAL", "CLASSICAL", "REGGAE", "COUNTRY", "FOLK",
    "PUNK", "FUNK", "TRAP", "SOUL", "LATIN", "K_POP", "INDIE", "ALTERNATIVE"
];

function CreateAlbumModal({isOpen, onClose}) {
    const { currentUser } = useAuth(); // Pobieramy usera, bo backend wymaga authorId

    const [step, setStep] = useState(1);
    const [isLoading, setIsLoading] = useState(false);

    // Dane Albumu
    const [albumTitle, setAlbumTitle] = useState("");
    const [selectedGenres, setSelectedGenres] = useState([]);
    const [isPublic, setIsPublic] = useState(false);

    // Obrazek
    const [imgSrc, setImgSrc] = useState(defaultAvatar);
    const [crop, setCrop] = useState();
    const imgRef = useRef(null);
    const [albumCoverBlob, setAlbumCoverBlob] = useState(null); // Tu trzymamy okładkę w pamięci

    // Wynik kroku 1
    const [createdAlbumId, setCreatedAlbumId] = useState(null);

    // Dane Piosenki
    const [songTitle, setSongTitle] = useState("");
    const [audioFile, setAudioFile] = useState(null);
    const [addedSongsCount, setAddedSongsCount] = useState(0);

    useEffect(() => {
        if (isOpen) {
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
        }
    }, [isOpen]);

    if (!isOpen) return null;

    // --- PLIKI ---
    const handleFileChange = (e, type) => {
        const file = e.target.files[0];
        if (!file) return;

        if (type === 'cover') {
            setImgSrc('');
            const reader = new FileReader();
            reader.onload = () => {
                setImgSrc(reader.result.toString() || '');
                setCrop(centerCrop(makeAspectCrop({unit: '%', width: 90}, 1, 100, 100), 100, 100));
            };
            reader.readAsDataURL(file);
        } else if (type === 'song') {
            if (!file.name.toLowerCase().endsWith('.m4a')) {
                alert("Tylko pliki .m4a!");
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
        const isSelected = selectedGenres.includes(genre);
        if (isSelected) setSelectedGenres(prev => prev.filter(g => g !== genre));
        else if (selectedGenres.length < 3) setSelectedGenres(prev => [...prev, genre]);
        else alert("Max 3 gatunki");
    };

    // --- KROK 1: CREATE ALBUM (JSON ONLY) ---
    const handleCreateAlbum = async () => {
        if (!albumTitle || !imgSrc || !crop || selectedGenres.length === 0) {
            alert("Uzupełnij nazwę, okładkę i gatunek albumu.");
            return;
        }

        if (!currentUser) {
            alert("Błąd: Brak zalogowanego użytkownika.");
            return;
        }

        setIsLoading(true);
        try {
            // 1. Zapisujemy obrazek w pamięci (Blob) na później
            const blob = await getCroppedImg(imgRef.current, crop);
            setAlbumCoverBlob(blob);

            // 2. Przygotowujemy JSON (zgodny z CreateAlbumRequest w Javie)
            const albumPayload = {
                title: albumTitle,
                description: albumTitle, // Opcjonalnie, backend wymaga description? Jeśli nie, można pominąć.
                authorId: currentUser.id, // Backend w AlbumService tego wymaga!
                publiclyVisible: isPublic
            };

            // 3. Wysyłamy JSON
            const albumDto = await createAlbum(albumPayload);

            console.log("Album utworzony (ID):", albumDto.id);
            setCreatedAlbumId(albumDto.id);
            setStep(2); // Przechodzimy dalej

        } catch (error) {
            console.error("Błąd tworzenia albumu:", error);
            alert("Nie udało się utworzyć albumu (Sprawdź konsolę).");
        } finally {
            setIsLoading(false);
        }
    };

    // --- KROK 2: ADD SONG (MULTIPART) ---
    const handleAddSong = async () => {
        if (!songTitle || !audioFile) {
            alert("Wybierz plik audio i tytuł.");
            return;
        }

        setIsLoading(true);
        try {
            const formData = new FormData();
            formData.append('audioFile', audioFile);
            formData.append('title', songTitle);

            // DOKLEJAMY OKŁADKĘ ALBUMU DO KAŻDEJ PIOSENKI
            if (albumCoverBlob) {
                formData.append('coverFile', albumCoverBlob, "cover.jpg");
            }

            // Dziedziczymy ustawienia albumu
            selectedGenres.forEach(g => formData.append('genre', g));
            formData.append('publiclyVisible', isPublic.toString());

            // Opcjonalnie (jeśli backend w SongController to obsługuje w DTO, a jak nie to i tak robimy addSongToAlbum poniżej)
            formData.append('albumId', createdAlbumId);

            // Upload piosenki
            const songRes = await api.post("/songs/upload", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });
            const songDto = songRes.data;

            // Powiązanie z albumem
            await addSongToAlbum(createdAlbumId, songDto.id);

            // Reset
            setSongTitle("");
            setAudioFile(null);
            document.getElementById('song-upload-step2').value = null;
            setAddedSongsCount(prev => prev + 1);
            alert(`Dodano: ${songDto.title}`);

        } catch (error) {
            console.error("Błąd piosenki:", error);
            alert("Nie udało się dodać piosenki.");
        } finally {
            setIsLoading(false);
        }
    };

    const handleFinish = () => {
        if (addedSongsCount === 0) {
            if(!window.confirm("Nie dodałeś żadnych piosenek. Zakończyć?")) return;
        }
        onClose();
    };

    return (
        <div className="edit-modal-backdrop" onClick={onClose}>
            <div className="edit-modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="close-button" onClick={onClose}>×</button>

                {step === 1 ? (
                    /* KROK 1: DANE ALBUMU */
                    <>
                        <h2>Stwórz Album (1/2)</h2>
                        <div className="edit-form">
                            <label>Nazwa albumu</label>
                            <input
                                type="text" placeholder="Tytuł"
                                value={albumTitle} onChange={e => setAlbumTitle(e.target.value)}
                            />

                            <label>Okładka (Zostanie przypisana do utworów)</label>
                            <div className="cover-art-buttons">
                                <label htmlFor="album-cover" className="file-upload-button">
                                    {imgSrc === defaultAvatar ? 'Wybierz okładkę' : 'Zmień'}
                                </label>
                                <input
                                    type="file" id="album-cover" className="file-upload-input"
                                    accept="image/*" onChange={(e) => handleFileChange(e, 'cover')}
                                />
                            </div>

                            {imgSrc !== defaultAvatar && (
                                <div className="cropper-container">
                                    <ReactCrop crop={crop} onChange={c => setCrop(c)} aspect={1}>
                                        <img ref={imgRef} src={imgSrc} alt="Cover" />
                                    </ReactCrop>
                                </div>
                            )}

                            <label>Gatunek (Wspólny dla utworów)</label>
                            <div className="genre-picker-container">
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

                            <button className="save-button" onClick={handleCreateAlbum} disabled={isLoading}>
                                {isLoading ? 'Tworzenie...' : 'Dalej: Dodaj piosenki'}
                            </button>
                        </div>
                    </>
                ) : (
                    /* KROK 2: PIOSENKI */
                    <>
                        <h2>Dodaj utwory do albumu</h2>
                        <div className="edit-form">
                            <div className="info-text">
                                Album utworzony! Piosenki automatycznie przejmą okładkę, gatunek i widoczność.
                            </div>

                            <label>Tytuł utworu</label>
                            <input
                                type="text" placeholder="Tytuł"
                                value={songTitle} onChange={e => setSongTitle(e.target.value)}
                            />

                            <label>Plik audio (.m4a)</label>
                            <label htmlFor="song-upload-step2" className="file-upload-button">
                                {audioFile ? audioFile.name : "Wybierz plik"}
                            </label>
                            <input
                                type="file" id="song-upload-step2" className="file-upload-input"
                                accept=".m4a" onChange={(e) => handleFileChange(e, 'song')}
                            />

                            <div style={{display:'flex', gap:'10px', marginTop:'20px'}}>
                                <button className="add-song-button-small" onClick={handleAddSong} disabled={isLoading}>
                                    {isLoading ? 'Dodawanie...' : '+ Dodaj utwór'}
                                </button>
                                <button className="finish-button" onClick={handleFinish} disabled={isLoading}>
                                    Zakończ ({addedSongsCount})
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