ALTER TABLE app_users
    ADD avatar_storage_key_id BIGINT DEFAULT 6767 NOT NULL;

ALTER TABLE app_users
    ALTER COLUMN avatar_storage_key_id SET NOT NULL;

ALTER TABLE app_users
    ADD CONSTRAINT FK_APP_USERS_ON_AVATAR_STORAGE_KEY FOREIGN KEY (avatar_storage_key_id) REFERENCES storage_keys (id);