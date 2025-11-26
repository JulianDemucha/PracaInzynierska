ALTER TABLE album_songs
    DROP CONSTRAINT fk_album_songs_on_album;

ALTER TABLE album_songs
    DROP CONSTRAINT fk_album_songs_on_song;

DROP TABLE album_songs CASCADE;