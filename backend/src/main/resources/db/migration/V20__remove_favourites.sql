ALTER TABLE favorites
    DROP CONSTRAINT fk_favorites_on_song;

ALTER TABLE favorites
    DROP CONSTRAINT fk_favorites_on_user;
DROP TABLE favorites CASCADE;
