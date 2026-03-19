DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'role_enum') THEN
    CREATE TYPE role_enum AS ENUM ('ADMIN', 'USER');
  END IF;
END $$;

CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role role_enum NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- seed admin user (password: admin)
INSERT INTO users (id,username, password, role, enabled)
VALUES (
  '08b5a3a9-8874-4fd7-b79a-45c877a65f6e',
  'admin',
  '$2a$10$OMyDYOcnjdO1cC/cbnqLyemYfqIdPw9elO.LSYOL6NGrt8gom98ei',
  'ADMIN',
  TRUE
) ON CONFLICT DO NOTHING;
