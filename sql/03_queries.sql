-- ============================================================
--  SyncStream – Useful SQL Queries  (Person 3 deliverable)
-- ============================================================
USE syncstream_db;

-- Q1: Get all active rooms with host name and participant count
SELECT
    r.room_id,
    r.room_code,
    r.room_name,
    u.username  AS host,
    COUNT(p.user_id) AS participant_count,
    r.created_at
FROM rooms r
JOIN users u ON r.host_user_id = u.user_id
LEFT JOIN participants p ON r.room_id = p.room_id AND p.left_at IS NULL
WHERE r.is_active = 1
GROUP BY r.room_id;

-- Q2: Get all chat messages in a room (ordered by time)
SELECT
    cm.message_id,
    u.username,
    cm.message,
    cm.msg_type,
    cm.sent_at
FROM chat_messages cm
JOIN users u ON cm.user_id = u.user_id
WHERE cm.room_id = ?         -- replace ? with the target room_id
ORDER BY cm.sent_at ASC;

-- Q3: Get watch history for a specific user
SELECT
    wh.history_id,
    r.room_name,
    wh.video_path,
    wh.watch_start,
    wh.watch_end,
    wh.total_seconds,
    CONCAT(FLOOR(wh.total_seconds / 60), ' min ', wh.total_seconds % 60, ' sec') AS duration
FROM watch_history wh
JOIN rooms r ON wh.room_id = r.room_id
WHERE wh.user_id = ?         -- replace ? with the target user_id
ORDER BY wh.watch_start DESC;

-- Q4: Top rooms by most participants ever
SELECT
    r.room_name,
    r.room_code,
    COUNT(DISTINCT p.user_id) AS total_users,
    u.username AS host
FROM rooms r
JOIN participants p  ON r.room_id = p.room_id
JOIN users u         ON r.host_user_id = u.user_id
GROUP BY r.room_id
ORDER BY total_users DESC
LIMIT 10;

-- Q5: Most active chatters across the platform
SELECT
    u.username,
    COUNT(*) AS total_messages
FROM chat_messages cm
JOIN users u ON cm.user_id = u.user_id
WHERE cm.msg_type = 'text'
GROUP BY u.user_id
ORDER BY total_messages DESC
LIMIT 10;

-- Q6: Average watch time per room
SELECT
    r.room_name,
    ROUND(AVG(wh.total_seconds) / 60, 2) AS avg_watch_minutes
FROM watch_history wh
JOIN rooms r ON wh.room_id = r.room_id
GROUP BY r.room_id
ORDER BY avg_watch_minutes DESC;

-- Q7: Verify login credentials
SELECT user_id, username, email
FROM users
WHERE username = ?
  AND password = SHA2(?, 256)
  AND is_active = 1;
