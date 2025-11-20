CREATE TABLE song_genres
(
    song_id BIGINT       NOT NULL,
    genre   VARCHAR(255) NOT NULL
);

ALTER TABLE song_genres
    ADD CONSTRAINT fk_song_genres_on_song FOREIGN KEY (song_id) REFERENCES songs (id);

INSERT INTO song_genres (song_id, genre)
SELECT id, genre
FROM songs
WHERE genre IS NOT NULL;

ALTER TABLE songs
    DROP COLUMN genre;