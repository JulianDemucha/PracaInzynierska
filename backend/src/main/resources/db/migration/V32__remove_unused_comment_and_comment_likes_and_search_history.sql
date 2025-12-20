ALTER TABLE comment_likes
    DROP CONSTRAINT fk_comment_likes_on_comment;

ALTER TABLE comment_likes
    DROP CONSTRAINT fk_comment_likes_on_user;

ALTER TABLE comments
    DROP CONSTRAINT fk_comments_on_song;

ALTER TABLE comments
    DROP CONSTRAINT fk_comments_on_user;

ALTER TABLE search_history
    DROP CONSTRAINT fk_search_history_on_user;

DROP TABLE comment_likes CASCADE;

DROP TABLE comments CASCADE;

DROP TABLE search_history CASCADE;