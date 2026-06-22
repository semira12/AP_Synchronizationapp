-- ============================================================
--  SyncStream – Sample Seed Data  (for testing)
-- ============================================================
USE syncstream_db;

-- Sample users  (passwords are SHA-256 of "password123")
INSERT INTO users (username, email, password) VALUES
('alice',   'alice@example.com',   SHA2('password123', 256)),
('bob',     'bob@example.com',     SHA2('password123', 256)),
('charlie', 'charlie@example.com', SHA2('password123', 256));

-- Sample room created by alice (user_id = 1)
INSERT INTO rooms (room_code, room_name, host_user_id, video_path) VALUES
('ROOM01', 'Movie Night', 1, '/videos/sample.mp4');

-- Participants
INSERT INTO participants (room_id, user_id, role) VALUES
(1, 1, 'host'),
(1, 2, 'viewer'),
(1, 3, 'viewer');

-- Sample chat messages
INSERT INTO chat_messages (room_id, user_id, message, msg_type) VALUES
(1, 1, 'Welcome everyone!',  'text'),
(1, 2, 'Hi Alice 👋',        'text'),
(1, 3, '❤️',                 'reaction'),
(1, 1, 'Starting in 5...',  'text');

-- Watch history
INSERT INTO watch_history (user_id, room_id, video_path, watch_start, watch_end, total_seconds) VALUES
(1, 1, '/videos/sample.mp4', NOW() - INTERVAL 1 HOUR, NOW(), 3600),
(2, 1, '/videos/sample.mp4', NOW() - INTERVAL 1 HOUR, NOW(), 3540),
(3, 1, '/videos/sample.mp4', NOW() - INTERVAL 50 MINUTE, NOW(), 3000);
