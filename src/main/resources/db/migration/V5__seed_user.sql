-- seed regular user (password: user)
-- Hash generated with BCryptPasswordEncoder(strength=10)
-- To regenerate: run SenhaUtil.main() with the desired password
INSERT INTO users (id, username, password, role, enabled)
VALUES (
    'b3f1e2d4-1234-4abc-9def-000000000001',
    'user',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyf3gv3ce',
    'USER',
    TRUE
) ON CONFLICT DO NOTHING;
