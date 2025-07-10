INSERT INTO users (email, password, first_name, last_name, provider, enabled, created_at)
VALUES (
           'admin@gmail.com',
           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfOUNHJdtNdOPf6',
           'Admin',
           'Lii',
           'LOCAL',
           true,
           CURRENT_TIMESTAMP
       );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@gmail.com'
  AND r.name = 'ROLE_ADMIN';


CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);