ALTER TABLE recommendations
    DROP CONSTRAINT fk_recommendations_on_song;

ALTER TABLE recommendations
    DROP CONSTRAINT fk_recommendations_on_user;

DROP TABLE recommendations CASCADE;
