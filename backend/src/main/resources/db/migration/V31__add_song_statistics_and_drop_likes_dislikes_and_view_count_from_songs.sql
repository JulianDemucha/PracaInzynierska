CREATE TABLE song_statistics
(
    song_id        BIGINT  NOT NULL,
    likes_count    INTEGER NOT NULL DEFAULT 0,
    dislikes_count INTEGER NOT NULL DEFAULT 0,
    view_count     BIGINT  NOT NULL DEFAULT 0,
    CONSTRAINT pk_song_statistics PRIMARY KEY (song_id)
);

INSERT INTO song_statistics (song_id, likes_count, dislikes_count, view_count)
SELECT
    s.id,
    (SELECT COUNT(*) FROM song_reactions r WHERE r.song_id = s.id AND r.reaction_type = 'LIKE'),
    (SELECT COUNT(*) FROM song_reactions r WHERE r.song_id = s.id AND r.reaction_type = 'DISLIKE'),
    COALESCE(s.view_count, 0)
FROM songs s;

ALTER TABLE song_statistics
    ADD CONSTRAINT FK_SONG_STATISTICS_ON_SONG
        FOREIGN KEY (song_id) REFERENCES songs (id);

ALTER TABLE songs DROP COLUMN dislikes_count;
ALTER TABLE songs DROP COLUMN likes_count;
ALTER TABLE songs DROP COLUMN view_count;