ALTER TABLE playlists
    ADD cover_storage_key_id BIGINT;

ALTER TABLE playlists
    ADD publicly_visible BOOLEAN;

ALTER TABLE playlists
    ALTER COLUMN cover_storage_key_id SET NOT NULL;

ALTER TABLE playlists
    ALTER COLUMN publicly_visible SET NOT NULL;

ALTER TABLE playlists
    ADD CONSTRAINT FK_PLAYLISTS_ON_COVER_STORAGE_KEY FOREIGN KEY (cover_storage_key_id) REFERENCES storage_keys (id);

ALTER TABLE playlists
    DROP COLUMN visibility;