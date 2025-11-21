ALTER TABLE songs
    RENAME COLUMN size_bytes TO audio_size_bytes;

ALTER TABLE songs
    RENAME COLUMN storage_key TO audio_storage_key;

ALTER TABLE songs
    ADD COLUMN cover_size_bytes BIGINT;

ALTER TABLE songs
    ADD COLUMN cover_storage_key VARCHAR(255);