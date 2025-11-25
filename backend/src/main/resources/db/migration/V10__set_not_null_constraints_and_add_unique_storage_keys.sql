ALTER TABLE storage_keys
    ADD CONSTRAINT uc_storage_keys_key UNIQUE (key);

ALTER TABLE songs
    ALTER COLUMN audio_storage_key_id SET NOT NULL;

ALTER TABLE songs
    ALTER COLUMN cover_storage_key_id SET NOT NULL;

ALTER TABLE storage_keys
    ALTER COLUMN mime_type SET NOT NULL;

ALTER TABLE albums
    ALTER COLUMN publicly_visible TYPE BOOLEAN USING (publicly_visible::BOOLEAN);

ALTER TABLE storage_keys
    ALTER COLUMN size_bytes SET NOT NULL;

ALTER TABLE songs
    ALTER COLUMN title SET NOT NULL;

ALTER TABLE songs
    ALTER COLUMN user_id SET NOT NULL;