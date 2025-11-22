ALTER TABLE songs
    RENAME COLUMN mime_type TO audio_file_mime_type;

ALTER TABLE songs
    ADD cover_file_mime_type VARCHAR(255);

UPDATE songs
SET cover_file_mime_type = 'image/jpeg';