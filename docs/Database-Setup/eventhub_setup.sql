-- =============================================
-- EventHub Campus Event Management System
-- MySQL 8.0 Database Setup Script
-- =============================================

SELECT '🚀 Starting EventHub Database Setup...' AS Status;
SELECT NOW() AS Started_At;

-- =============================================
-- DATABASE SETUP
-- =============================================

SELECT '📁 Setting up database...' AS Progress;

DROP DATABASE IF EXISTS eventhub_db;
CREATE DATABASE eventhub_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eventhub_db;

SET FOREIGN_KEY_CHECKS = 0;
SET AUTOCOMMIT = 0;

SELECT '✅ Database created successfully!' AS Status;

-- =============================================
-- USER MANAGEMENT TABLES
-- =============================================

SELECT '👤 Creating user management tables...' AS Progress;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    user_type ENUM('student', 'organiser', 'admin') DEFAULT 'student',
    profile_picture VARCHAR(500),
    bio TEXT,
    interests JSON,
    is_active BOOLEAN DEFAULT TRUE,
    is_banned BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB;

CREATE TABLE clubs (
    club_id INT AUTO_INCREMENT PRIMARY KEY,
    club_name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    website_url VARCHAR(500),
    contact_email VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_club_name (club_name)
) ENGINE=InnoDB;

CREATE TABLE club_members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    club_id INT NOT NULL,
    user_id INT NOT NULL,
    role ENUM('president', 'vice_president', 'secretary', 'treasurer', 'member') DEFAULT 'member',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (club_id) REFERENCES clubs(club_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_club_user (club_id, user_id),
    INDEX idx_user_clubs (user_id)
) ENGINE=InnoDB;

SELECT '✅ User management tables created!' AS Status;

-- =============================================
-- EVENT MANAGEMENT TABLES
-- =============================================

SELECT '🎪 Creating event management tables...' AS Progress;

CREATE TABLE event_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon VARCHAR(100),
    colour_code VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(500) NOT NULL,
    venue_details TEXT,
    max_capacity INT,
    current_rsvp_count INT DEFAULT 0,
    category_id INT,
    club_id INT NOT NULL,
    created_by INT NOT NULL,
    cover_image VARCHAR(500),
    registration_deadline DATETIME,
    is_paid BOOLEAN DEFAULT FALSE,
    price DECIMAL(10, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'AUD',
    status ENUM('draft', 'published', 'cancelled', 'completed') DEFAULT 'draft',
    is_featured BOOLEAN DEFAULT FALSE,
    google_maps_url VARCHAR(1000),
    qr_code_data VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES event_categories(category_id) ON DELETE SET NULL,
    FOREIGN KEY (club_id) REFERENCES clubs(club_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_event_date (event_date),
    INDEX idx_status (status),
    INDEX idx_club_events (club_id),
    FULLTEXT idx_search (title, description)
) ENGINE=InnoDB;

CREATE TABLE event_tags (
    tag_id INT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(50) UNIQUE NOT NULL,
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tag_name (tag_name)
) ENGINE=InnoDB;

CREATE TABLE event_tag_mappings (
    event_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (event_id, tag_id),
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES event_tags(tag_id) ON DELETE CASCADE
) ENGINE=InnoDB;

SELECT '✅ Event management tables created!' AS Status;

-- =============================================
-- USER PARTICIPATION TABLES
-- =============================================

SELECT '📅 Creating user participation tables...' AS Progress;

CREATE TABLE rsvps (
    rsvp_id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    rsvp_status ENUM('yes', 'no', 'maybe', 'cancelled') DEFAULT 'yes',
    attendance_status ENUM('not_checked', 'checked_in', 'no_show') DEFAULT 'not_checked',
    check_in_time TIMESTAMP NULL,
    qr_code VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_event_user (event_id, user_id),
    INDEX idx_user_rsvps (user_id),
    INDEX idx_event_rsvps (event_id)
) ENGINE=InnoDB;

CREATE TABLE event_feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comments TEXT,
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_event_user_feedback (event_id, user_id),
    INDEX idx_event_feedback (event_id)
) ENGINE=InnoDB;

CREATE TABLE event_gallery (
    photo_id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    caption TEXT,
    uploaded_by INT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_featured BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_event_photos (event_id)
) ENGINE=InnoDB;

SELECT '✅ User participation tables created!' AS Status;

-- =============================================
-- NOTIFICATIONS & SYSTEM TABLES
-- =============================================

SELECT '🔔 Creating notifications and system tables...' AS Progress;

CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type ENUM('event_reminder', 'rsvp_confirmation', 'event_update', 'event_cancelled', 'new_event', 'feedback_request') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_event_id INT,
    is_read BOOLEAN DEFAULT FALSE,
    is_email_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (related_event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    INDEX idx_user_notifications (user_id, is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

CREATE TABLE event_reminders (
    reminder_id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    reminder_type ENUM('email', 'notification', 'both') DEFAULT 'both',
    reminder_time_before INT NOT NULL,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    INDEX idx_event_reminders (event_id)
) ENGINE=InnoDB;

CREATE TABLE audit_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    action_type ENUM('delete_event', 'ban_user', 'unban_user', 'deactivate_user', 'modify_event', 'delete_feedback') NOT NULL,
    target_type ENUM('user', 'event', 'feedback', 'club') NOT NULL,
    target_id INT NOT NULL,
    details JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_admin_actions (admin_id),
    INDEX idx_action_type (action_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

SELECT '✅ Notifications and system tables created!' AS Status;

-- =============================================
-- EXTENSION FEATURES
-- =============================================

SELECT '💰 Creating extension feature tables...' AS Progress;

CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'AUD',
    payment_method ENUM('credit_card', 'debit_card', 'paypal', 'stripe') NOT NULL,
    transaction_id VARCHAR(255) UNIQUE,
    payment_status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    refund_date TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE RESTRICT,
    INDEX idx_user_payments (user_id),
    INDEX idx_event_payments (event_id),
    INDEX idx_transaction (transaction_id)
) ENGINE=InnoDB;

CREATE TABLE badges (
    badge_id INT AUTO_INCREMENT PRIMARY KEY,
    badge_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    criteria JSON,
    badge_type ENUM('attendance', 'participation', 'special', 'milestone') NOT NULL,
    points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE user_badges (
    user_badge_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    badge_id INT NOT NULL,
    earned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(badge_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_badge (user_id, badge_id),
    INDEX idx_user_badges (user_id)
) ENGINE=InnoDB;

CREATE TABLE api_keys (
    key_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    api_key VARCHAR(64) UNIQUE NOT NULL,
    api_secret VARCHAR(128) NOT NULL,
    name VARCHAR(100),
    permissions JSON,
    is_active BOOLEAN DEFAULT TRUE,
    last_used TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_api_key (api_key),
    INDEX idx_user_keys (user_id)
) ENGINE=InnoDB;

CREATE TABLE api_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    api_key VARCHAR(64),
    endpoint VARCHAR(255),
    method VARCHAR(10),
    status_code INT,
    response_time_ms INT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_api_key_logs (api_key),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

SELECT '✅ Extension feature tables created!' AS Status;

-- =============================================
-- VIEWS AND STORED PROCEDURES
-- =============================================

SELECT '📊 Creating views and stored procedures...' AS Progress;

CREATE VIEW upcoming_events_view AS
SELECT 
    e.event_id,
    e.title,
    e.description,
    e.event_date,
    e.start_time,
    e.end_time,
    e.location,
    e.max_capacity,
    e.current_rsvp_count,
    e.is_paid,
    e.price,
    e.status,
    c.club_name,
    cat.category_name,
    u.username AS created_by_username,
    GROUP_CONCAT(DISTINCT t.tag_name) AS tags
FROM events e
LEFT JOIN clubs c ON e.club_id = c.club_id
LEFT JOIN event_categories cat ON e.category_id = cat.category_id
LEFT JOIN users u ON e.created_by = u.user_id
LEFT JOIN event_tag_mappings etm ON e.event_id = etm.event_id
LEFT JOIN event_tags t ON etm.tag_id = t.tag_id
WHERE e.event_date >= CURDATE() 
    AND e.status = 'published'
GROUP BY e.event_id
ORDER BY e.event_date, e.start_time;

CREATE VIEW user_participation_stats AS
SELECT 
    u.user_id,
    u.username,
    COUNT(DISTINCT r.event_id) AS events_attended,
    COUNT(DISTINCT ub.badge_id) AS badges_earned,
    AVG(f.rating) AS avg_feedback_rating
FROM users u
LEFT JOIN rsvps r ON u.user_id = r.user_id AND r.attendance_status = 'checked_in'
LEFT JOIN user_badges ub ON u.user_id = ub.user_id
LEFT JOIN event_feedback f ON u.user_id = f.user_id
GROUP BY u.user_id;

DELIMITER //

CREATE PROCEDURE update_event_rsvp_count(IN p_event_id INT)
BEGIN
    UPDATE events 
    SET current_rsvp_count = (
        SELECT COUNT(*) 
        FROM rsvps 
        WHERE event_id = p_event_id 
            AND rsvp_status = 'yes'
    )
    WHERE event_id = p_event_id;
END//

CREATE PROCEDURE check_in_user(
    IN p_event_id INT,
    IN p_user_id INT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_rsvp_exists INT;
    
    SELECT COUNT(*) INTO v_rsvp_exists
    FROM rsvps
    WHERE event_id = p_event_id AND user_id = p_user_id AND rsvp_status = 'yes';
    
    IF v_rsvp_exists > 0 THEN
        UPDATE rsvps
        SET attendance_status = 'checked_in',
            check_in_time = CURRENT_TIMESTAMP
        WHERE event_id = p_event_id AND user_id = p_user_id;
        
        SET p_success = TRUE;
        SET p_message = 'Check-in successful';
    ELSE
        SET p_success = FALSE;
        SET p_message = 'No RSVP found for this user';
    END IF;
END//

DELIMITER ;

DELIMITER //

CREATE TRIGGER after_rsvp_insert
AFTER INSERT ON rsvps
FOR EACH ROW
BEGIN
    IF NEW.rsvp_status = 'yes' THEN
        CALL update_event_rsvp_count(NEW.event_id);
    END IF;
END//

CREATE TRIGGER after_rsvp_update
AFTER UPDATE ON rsvps
FOR EACH ROW
BEGIN
    IF OLD.rsvp_status != NEW.rsvp_status THEN
        CALL update_event_rsvp_count(NEW.event_id);
    END IF;
END//

CREATE TRIGGER after_event_tag_insert
AFTER INSERT ON event_tag_mappings
FOR EACH ROW
BEGIN
    UPDATE event_tags 
    SET usage_count = usage_count + 1 
    WHERE tag_id = NEW.tag_id;
END//

DELIMITER ;

SELECT '✅ Views and stored procedures created!' AS Status;

-- =============================================
-- PERFORMANCE INDEXES
-- =============================================

SELECT '⚡ Creating performance indexes...' AS Progress;

CREATE INDEX idx_events_date_status ON events(event_date, status);
CREATE INDEX idx_rsvps_status ON rsvps(rsvp_status, attendance_status);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read, created_at);

SELECT '✅ Performance indexes created!' AS Status;

-- =============================================
-- INITIAL DATA
-- =============================================

SELECT '📝 Inserting initial data...' AS Progress;

INSERT INTO event_categories (category_name, description, colour_code) VALUES
('Academic', 'Lectures, workshops, and study sessions', '#4CAF50'),
('Social', 'Parties, gatherings, and social events', '#2196F3'),
('Sports', 'Sports events and competitions', '#FF9800'),
('Career', 'Career fairs, networking, and professional development', '#9C27B0'),
('Arts & Culture', 'Cultural events, exhibitions, and performances', '#E91E63'),
('Technology', 'Tech talks, hackathons, and coding workshops', '#00BCD4'),
('Music', 'Concerts, performances, and music events', '#FFC107'),
('Volunteer', 'Community service and volunteer opportunities', '#8BC34A');

INSERT INTO badges (badge_name, description, badge_type, points) VALUES
('First Timer', 'Attended your first event', 'milestone', 10),
('Frequent Attendee', 'Attended 10 events', 'attendance', 50),
('Super Attendee', 'Attended 25 events', 'attendance', 100),
('Social Butterfly', 'Attended 5 social events', 'participation', 30),
('Tech Enthusiast', 'Attended 5 technology events', 'participation', 30),
('Feedback Champion', 'Provided feedback for 10 events', 'participation', 40),
('Early Bird', 'RSVP to 5 events more than a week in advance', 'special', 25);

INSERT INTO users (email, username, password_hash, first_name, last_name, user_type, email_verified) 
VALUES ('admin@eventhub.edu', 'admin', '$2y$10$example_hash_change_in_production', 'System', 'Administrator', 'admin', TRUE);

SELECT '✅ Initial data inserted!' AS Status;

-- =============================================
-- FINAL SETUP
-- =============================================

SELECT '🔧 Finalising setup...' AS Progress;

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;

SELECT 'EventHub Database Setup Complete!' AS Status;
SELECT NOW() AS Completed_At;
SELECT DATABASE() AS Current_Database;
SELECT COUNT(*) AS Total_Tables FROM information_schema.tables WHERE table_schema = 'eventhub_db';

SELECT 'Database Summary:' AS Info;
SELECT 'Event Categories:' AS Type, COUNT(*) AS Count FROM event_categories
UNION ALL
SELECT 'Default Badges:', COUNT(*) FROM badges
UNION ALL
SELECT 'Admin Users:', COUNT(*) FROM users WHERE user_type = 'admin';

SELECT 'Ready for development!' AS Final_Status;