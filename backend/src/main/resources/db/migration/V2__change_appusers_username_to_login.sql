ALTER TABLE app_users
    ADD login VARCHAR(255);

UPDATE app_users
    SET login = username;

ALTER TABLE app_users
    ALTER COLUMN login SET NOT NULL;

ALTER TABLE app_users
    ADD CONSTRAINT uc_app_users_login UNIQUE (login);

ALTER TABLE app_users
    DROP COLUMN username;