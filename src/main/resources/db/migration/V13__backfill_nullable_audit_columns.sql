UPDATE free_games
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE user_profile
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

UPDATE refresh_token
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP);

UPDATE game_managers
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP);

ALTER TABLE free_games
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE free_games
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE user_profile
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE user_profile
    ALTER COLUMN updated_at SET NOT NULL;
