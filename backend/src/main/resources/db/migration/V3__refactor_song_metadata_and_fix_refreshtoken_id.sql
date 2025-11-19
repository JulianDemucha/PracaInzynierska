ALTER TABLE songs
    ADD mime_type VARCHAR(255);

ALTER TABLE songs
    ADD publicly_visible BOOLEAN;

ALTER TABLE songs
    ADD size_bytes BIGINT;

ALTER TABLE songs
    ADD storage_key VARCHAR(255);

ALTER TABLE songs
    ALTER COLUMN publicly_visible SET NOT NULL;

ALTER TABLE songs
    DROP COLUMN file_path;

ALTER TABLE songs
    DROP COLUMN visibility;

ALTER TABLE refresh_token
    ALTER COLUMN id TYPE BIGINT USING (id:: BIGINT);