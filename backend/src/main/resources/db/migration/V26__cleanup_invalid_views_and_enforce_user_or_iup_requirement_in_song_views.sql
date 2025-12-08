BEGIN;

WITH bad_views_counts AS (
    SELECT song_id, COUNT(*) as count_to_remove
    FROM song_views
    WHERE user_id IS NULL AND ip_address IS NULL
    GROUP BY song_id
)

UPDATE songs s
SET view_count = GREATEST(s.view_count - bvc.count_to_remove, 0) -- greatest viewCount-1,0 w razie jakiegos bledu
FROM bad_views_counts bvc
WHERE s.id = bvc.song_id;

DELETE FROM song_views
WHERE user_id IS NULL AND ip_address IS NULL;

ALTER TABLE song_views
    ADD CONSTRAINT chk_user_or_ip_required
        CHECK (user_id IS NOT NULL OR ip_address IS NOT NULL);

COMMIT;