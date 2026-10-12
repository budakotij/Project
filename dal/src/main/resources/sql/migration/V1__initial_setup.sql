CREATE TABLE users (
    user_name         VARCHAR(40) NOT NULL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    email             VARCHAR(255) UNIQUE,
    email_verified    BOOL NOT NULL DEFAULT false,
    password_hash     VARCHAR(255) NOT NULL,
    profile_image     VARCHAR(500),
    user_type         VARCHAR(50) NOT NULL,
    registration_date TIMESTAMP NOT NULL,
    is_active         BOOL NOT NULL DEFAULT true,
    roles             TEXT[]
);