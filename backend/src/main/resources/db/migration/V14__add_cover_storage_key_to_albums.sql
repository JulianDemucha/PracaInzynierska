ALTER TABLE albums
    ADD cover_storage_key_id BIGINT DEFAULT 6767 NOT NULL;

ALTER TABLE albums
    ADD CONSTRAINT FK_ALBUMS_ON_COVER_STORAGE_KEY
        FOREIGN KEY (cover_storage_key_id) REFERENCES storage_keys (id);
