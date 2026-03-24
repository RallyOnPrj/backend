CREATE TABLE identity_oauth_links (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
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
    CONSTRAINT fk_identity_oauth_links_user FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT uq_identity_oauth_links_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT uq_identity_oauth_links_user_provider UNIQUE (user_id, provider)
);

CREATE INDEX idx_identity_oauth_links_user_id ON identity_oauth_links (user_id);

INSERT INTO identity_oauth_links (
    id,
    user_id,
    provider,
    provider_user_id,
    email,
    nickname,
    profile_image_url,
    thumbnail_image_url,
    gender,
    age_range,
    birthday,
    is_email_verified,
    is_phone_number_verified,
    created_at,
    updated_at
)
SELECT
    id,
    user_id,
    provider,
    provider_user_id,
    email,
    nickname,
    profile_image_url,
    thumbnail_image_url,
    gender,
    age_range,
    birthday,
    is_email_verified,
    is_phone_number_verified,
    COALESCE(created_at, CURRENT_TIMESTAMP),
    COALESCE(updated_at, CURRENT_TIMESTAMP)
FROM user_auth;

UPDATE identity_oauth_links links
SET email = COALESCE(links.email, legacy.email),
    nickname = COALESCE(links.nickname, legacy.nickname),
    profile_image_url = COALESCE(links.profile_image_url, legacy.profile_image_url)
FROM oauth_users legacy
WHERE legacy.oauth_provider = links.provider
  AND legacy.oauth_id = links.provider_user_id;

CREATE TABLE identity_local_credentials (
    user_id UUID PRIMARY KEY,
    email_normalized VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_identity_local_credentials_user FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT uq_identity_local_credentials_email UNIQUE (email_normalized)
);

DROP TABLE court_tournament_participants;
DROP TABLE court_tournaments;
DROP TABLE oauth_users;
DROP TABLE user_auth;
