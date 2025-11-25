CREATE TABLE album_genres
(
    album_id BIGINT       NOT NULL,
    genre    VARCHAR(255) DEFAULT 'POP' NOT NULL
);

ALTER TABLE album_genres
    ADD CONSTRAINT fk_album_genres_on_album FOREIGN KEY (album_id) REFERENCES albums (id);