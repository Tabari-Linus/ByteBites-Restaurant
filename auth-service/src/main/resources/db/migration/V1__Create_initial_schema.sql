
CREATE TABLE users (
                       id UUID DEFAULT gen_random_uuid() NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255),
                       first_name VARCHAR(255) NOT NULL,
                       last_name VARCHAR(255) NOT NULL,
                       provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
                       enabled BOOLEAN NOT NULL DEFAULT true,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       last_login_at TIMESTAMP,
                       PRIMARY KEY (id)
);

CREATE TABLE roles (
                       id UUID DEFAULT gen_random_uuid() NOT NULL,
                       name VARCHAR(50) UNIQUE NOT NULL,
                       PRIMARY KEY (id)
);

CREATE TABLE user_roles (
                            user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                            role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
                                id UUID DEFAULT gen_random_uuid() NOT NULL,
                                token VARCHAR(255) UNIQUE NOT NULL,
                                user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                                expiry_date TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (id)
);

INSERT INTO roles (id, name) VALUES
                                 (gen_random_uuid(), 'ROLE_CUSTOMER'),
                                 (gen_random_uuid(), 'ROLE_RESTAURANT_OWNER'),
                                 (gen_random_uuid(), 'ROLE_ADMIN');