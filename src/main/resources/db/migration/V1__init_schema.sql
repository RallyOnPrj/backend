CREATE TABLE identity_accounts (
    id UUID PRIMARY KEY,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE identity_local_credentials (
    identity_account_id UUID PRIMARY KEY,
    email_normalized VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_identity_local_credentials_account
        FOREIGN KEY (identity_account_id) REFERENCES identity_accounts(id),
    CONSTRAINT uq_identity_local_credentials_email UNIQUE (email_normalized)
);

CREATE TABLE identity_oauth_links (
    id UUID PRIMARY KEY,
    identity_account_id UUID NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    nickname VARCHAR(255),
    profile_image_url VARCHAR(500),
    thumbnail_image_url VARCHAR(500),
    gender VARCHAR(20),
    age_range VARCHAR(20),
    birthday VARCHAR(20),
    is_email_verified BOOLEAN,
    is_phone_number_verified BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_identity_oauth_links_account
        FOREIGN KEY (identity_account_id) REFERENCES identity_accounts(id),
    CONSTRAINT uq_identity_oauth_links_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT uq_identity_oauth_links_account_provider UNIQUE (identity_account_id, provider)
);

CREATE INDEX idx_identity_oauth_links_account_id
    ON identity_oauth_links (identity_account_id);

CREATE TABLE region_province (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_region_province_code UNIQUE (code)
);

CREATE TABLE region_district (
    id UUID PRIMARY KEY,
    province_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_region_district_province
        FOREIGN KEY (province_id) REFERENCES region_province(id)
);

CREATE INDEX idx_region_district_province_id
    ON region_district (province_id);

CREATE TABLE user_profile (
    identity_account_id UUID PRIMARY KEY,
    nickname VARCHAR(255),
    profile_image_url VARCHAR(255),
    birth TIMESTAMP,
    birth_visible BOOLEAN NOT NULL,
    regional_grade VARCHAR(255),
    national_grade VARCHAR(255),
    district_id UUID,
    gender VARCHAR(255),
    tag VARCHAR(4) NOT NULL,
    tag_changed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_profile_account
        FOREIGN KEY (identity_account_id) REFERENCES identity_accounts(id),
    CONSTRAINT fk_user_profile_district
        FOREIGN KEY (district_id) REFERENCES region_district(id),
    CONSTRAINT uq_user_profile_nickname_tag UNIQUE (nickname, tag)
);

CREATE INDEX idx_user_profile_nickname
    ON user_profile (nickname);

CREATE TABLE user_grade_history (
    id UUID PRIMARY KEY,
    identity_account_id UUID NOT NULL,
    grade VARCHAR(255),
    grade_type VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP,
    CONSTRAINT fk_user_grade_history_account
        FOREIGN KEY (identity_account_id) REFERENCES identity_accounts(id)
);

CREATE INDEX idx_user_grade_history_identity_account_id
    ON user_grade_history (identity_account_id);
CREATE INDEX idx_user_grade_history_changed_at
    ON user_grade_history (changed_at);

CREATE TABLE free_games (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    organizer_identity_account_id UUID NOT NULL,
    grade_type VARCHAR(255) NOT NULL,
    game_type VARCHAR(255) NOT NULL,
    game_status VARCHAR(255) NOT NULL,
    match_record_mode VARCHAR(255) NOT NULL,
    share_code VARCHAR(64),
    location VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_free_games_organizer
        FOREIGN KEY (organizer_identity_account_id) REFERENCES identity_accounts(id),
    CONSTRAINT uq_free_games_share_code UNIQUE (share_code)
);

CREATE TABLE free_game_settings (
    id UUID PRIMARY KEY,
    freegame_id UUID NOT NULL,
    court_count INTEGER NOT NULL,
    round_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_free_game_settings_free_game
        FOREIGN KEY (freegame_id) REFERENCES free_games(id),
    CONSTRAINT uq_free_game_settings_freegame UNIQUE (freegame_id)
);

CREATE TABLE game_participants (
    id UUID PRIMARY KEY,
    freegame_id UUID NOT NULL,
    identity_account_id UUID,
    original_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    gender VARCHAR(255) NOT NULL,
    grade VARCHAR(255) NOT NULL,
    age_group INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_game_participants_free_game
        FOREIGN KEY (freegame_id) REFERENCES free_games(id),
    CONSTRAINT fk_game_participants_identity_account
        FOREIGN KEY (identity_account_id) REFERENCES identity_accounts(id),
    CONSTRAINT uq_game_participants_account_per_game UNIQUE (freegame_id, identity_account_id)
);

CREATE INDEX idx_game_participants_freegame_id
    ON game_participants (freegame_id);

CREATE TABLE game_managers (
    id UUID PRIMARY KEY,
    freegame_id UUID NOT NULL,
    identity_account_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_game_managers_free_game
        FOREIGN KEY (freegame_id) REFERENCES free_games(id),
    CONSTRAINT fk_game_managers_identity_account
        FOREIGN KEY (identity_account_id) REFERENCES identity_accounts(id),
    CONSTRAINT uq_game_managers_account_per_game UNIQUE (freegame_id, identity_account_id)
);

CREATE INDEX idx_game_managers_freegame_id
    ON game_managers (freegame_id);

CREATE TABLE free_game_round (
    id UUID PRIMARY KEY,
    freegame_id UUID NOT NULL,
    round_number INTEGER NOT NULL,
    round_status VARCHAR(255),
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_free_game_round_free_game
        FOREIGN KEY (freegame_id) REFERENCES free_games(id),
    CONSTRAINT uq_free_game_round_number UNIQUE (freegame_id, round_number)
);

CREATE INDEX idx_free_game_round_freegame_id
    ON free_game_round (freegame_id);

CREATE TABLE free_game_match (
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL,
    court_number INTEGER NOT NULL,
    team_a_player1_id UUID,
    team_a_player2_id UUID,
    team_b_player1_id UUID,
    team_b_player2_id UUID,
    match_type VARCHAR(255),
    match_status VARCHAR(255),
    match_result VARCHAR(255),
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_free_game_match_round
        FOREIGN KEY (round_id) REFERENCES free_game_round(id),
    CONSTRAINT fk_free_game_match_team_a_player1
        FOREIGN KEY (team_a_player1_id) REFERENCES game_participants(id),
    CONSTRAINT fk_free_game_match_team_a_player2
        FOREIGN KEY (team_a_player2_id) REFERENCES game_participants(id),
    CONSTRAINT fk_free_game_match_team_b_player1
        FOREIGN KEY (team_b_player1_id) REFERENCES game_participants(id),
    CONSTRAINT fk_free_game_match_team_b_player2
        FOREIGN KEY (team_b_player2_id) REFERENCES game_participants(id),
    CONSTRAINT uq_free_game_match_round_court UNIQUE (round_id, court_number)
);

CREATE INDEX idx_free_game_match_round_id
    ON free_game_match (round_id);

CREATE TABLE oauth2_registered_client (
    id VARCHAR(100) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at TIMESTAMP DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL
);

CREATE UNIQUE INDEX idx_oauth2_registered_client_client_id
    ON oauth2_registered_client (client_id);

CREATE TABLE oauth2_authorization (
    id VARCHAR(100) PRIMARY KEY,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000) DEFAULT NULL,
    attributes TEXT DEFAULT NULL,
    state VARCHAR(500) DEFAULT NULL,
    authorization_code_value TEXT DEFAULT NULL,
    authorization_code_issued_at TIMESTAMP DEFAULT NULL,
    authorization_code_expires_at TIMESTAMP DEFAULT NULL,
    authorization_code_metadata TEXT DEFAULT NULL,
    access_token_value TEXT DEFAULT NULL,
    access_token_issued_at TIMESTAMP DEFAULT NULL,
    access_token_expires_at TIMESTAMP DEFAULT NULL,
    access_token_metadata TEXT DEFAULT NULL,
    access_token_type VARCHAR(100) DEFAULT NULL,
    access_token_scopes VARCHAR(1000) DEFAULT NULL,
    oidc_id_token_value TEXT DEFAULT NULL,
    oidc_id_token_issued_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_expires_at TIMESTAMP DEFAULT NULL,
    oidc_id_token_metadata TEXT DEFAULT NULL,
    refresh_token_value TEXT DEFAULT NULL,
    refresh_token_issued_at TIMESTAMP DEFAULT NULL,
    refresh_token_expires_at TIMESTAMP DEFAULT NULL,
    refresh_token_metadata TEXT DEFAULT NULL,
    user_code_value TEXT DEFAULT NULL,
    user_code_issued_at TIMESTAMP DEFAULT NULL,
    user_code_expires_at TIMESTAMP DEFAULT NULL,
    user_code_metadata TEXT DEFAULT NULL,
    device_code_value TEXT DEFAULT NULL,
    device_code_issued_at TIMESTAMP DEFAULT NULL,
    device_code_expires_at TIMESTAMP DEFAULT NULL,
    device_code_metadata TEXT DEFAULT NULL
);

CREATE INDEX idx_oauth2_authorization_registered_client_id
    ON oauth2_authorization (registered_client_id);
CREATE INDEX idx_oauth2_authorization_principal_name
    ON oauth2_authorization (principal_name);
CREATE INDEX idx_oauth2_authorization_state
    ON oauth2_authorization (state);
CREATE INDEX idx_oauth2_authorization_authorization_code_value
    ON oauth2_authorization (authorization_code_value);
CREATE INDEX idx_oauth2_authorization_access_token_value
    ON oauth2_authorization (access_token_value);
CREATE INDEX idx_oauth2_authorization_refresh_token_value
    ON oauth2_authorization (refresh_token_value);
CREATE INDEX idx_oauth2_authorization_user_code_value
    ON oauth2_authorization (user_code_value);
CREATE INDEX idx_oauth2_authorization_device_code_value
    ON oauth2_authorization (device_code_value);

CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);
