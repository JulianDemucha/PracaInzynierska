UPDATE songs s
SET
    likes_count = (
        SELECT COUNT(*)
        FROM song_reactions r
        WHERE r.song_id = s.id
          AND r.reaction_type = 'LIKE'
    ),
    dislikes_count = (
        SELECT COUNT(*)
        FROM song_reactions r
        WHERE r.song_id = s.id
          AND r.reaction_type = 'DISLIKE'
    );

ALTER TABLE songs
    ALTER COLUMN likes_count SET DEFAULT 0;

ALTER TABLE songs
    ALTER COLUMN dislikes_count SET DEFAULT 0;

ALTER TABLE songs
    ALTER COLUMN dislikes_count SET NOT NULL;

ALTER TABLE songs
    ALTER COLUMN likes_count SET NOT NULL;