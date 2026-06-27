-- ============================================================
-- GDB Users Seed Data
-- ============================================================
-- Credentials (password → bcrypt hash):
--   admin    / Admin@1234  → hash below
--   manager1 / Manager@1234
--   teller1  / Teller@1234
--   teller2  / Teller@1234
-- ============================================================

-- Additional ADMIN
INSERT INTO users (username, login_id, password, role, is_active)
VALUES ('Priya Sharma', 'admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlZz.2y5DYVyp2', 'ADMIN', TRUE)
ON CONFLICT (login_id) DO UPDATE
  SET username = EXCLUDED.username,
      is_active = TRUE;

-- MANAGER
INSERT INTO users (username, login_id, password, role, is_active)
VALUES
  ('Rajesh Kumar', 'manager1', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlZz.2y5DYVyp2', 'MANAGER', TRUE),
  ('Sunita Agarwal', 'manager2', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlZz.2y5DYVyp2', 'MANAGER', TRUE)
ON CONFLICT (login_id) DO NOTHING;

-- TELLERS
INSERT INTO users (username, login_id, password, role, is_active)
VALUES
  ('Amit Verma', 'teller1', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlZz.2y5DYVyp2', 'TELLER', TRUE),
  ('Neha Singh', 'teller2', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlZz.2y5DYVyp2', 'TELLER', TRUE),
  ('Vikram Patel', 'teller3', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlZz.2y5DYVyp2', 'TELLER', FALSE)
ON CONFLICT (login_id) DO NOTHING;
