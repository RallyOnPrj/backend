ALTER TABLE free_games
    ADD CONSTRAINT uq_free_games_share_code UNIQUE (share_code);
