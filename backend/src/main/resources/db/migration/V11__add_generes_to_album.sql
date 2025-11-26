CREATE TABLE album_genres
(
    album_id BIGINT       NOT NULL,
    genre    VARCHAR(255) NOT NULL
);

ALTER TABLE album_genres
    ADD CONSTRAINT fk_album_genres_on_album FOREIGN KEY (album_id) REFERENCES albums (id);

INSERT INTO album_genres (album_id, genre)
SELECT id, 'POP'
FROM albums;