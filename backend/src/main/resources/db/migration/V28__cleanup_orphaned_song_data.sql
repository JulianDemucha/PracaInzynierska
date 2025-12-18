DELETE FROM song_views
WHERE song_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM songs
    WHERE songs.id = song_views.song_id
);

DELETE FROM song_reactions
WHERE song_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM songs
    WHERE songs.id = song_reactions.song_id
);