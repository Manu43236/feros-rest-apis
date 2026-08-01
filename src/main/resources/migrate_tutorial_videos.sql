CREATE TABLE IF NOT EXISTS tutorial_videos (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    role          VARCHAR(50)  NOT NULL COMMENT 'DRIVER, CLEANER, SUPERVISOR, TECHNICIAN, SERVICE_MANAGER, STORE_KEEPER, OFFICE_STAFF, ADMIN, ALL',
    language      VARCHAR(10)  NOT NULL DEFAULT 'te',
    feature_title VARCHAR(150) NOT NULL,
    youtube_url   VARCHAR(500) NOT NULL,
    sort_order    INT          NOT NULL DEFAULT 0,
    is_active     TINYINT(1)  NOT NULL DEFAULT 1,
    created_at    DATETIME,
    updated_at    DATETIME
);

INSERT INTO tutorial_videos (role, language, feature_title, youtube_url, sort_order, is_active, created_at, updated_at) VALUES
('ALL',     'te', 'Login చేయడం ఎలా',      'https://www.youtube.com/shorts/rHpqpKdqiTM', 0, 1, NOW(), NOW()),
('DRIVER',  'te', 'Trip Start చేయడం ఎలా', 'https://www.youtube.com/shorts/uj_axMjujH0', 1, 1, NOW(), NOW()),
('DRIVER',  'te', 'Trip End చేయడం ఎలా',   'https://youtube.com/shorts/E2wa0ji_jiA',     2, 1, NOW(), NOW()),
('DRIVER',  'te', 'Mark Out చేయడం ఎలా',   'https://youtube.com/shorts/IDqOjBfYAW4',     3, 1, NOW(), NOW()),
('CLEANER', 'te', 'Mark Out చేయడం ఎలా',   'https://youtube.com/shorts/IDqOjBfYAW4',     1, 1, NOW(), NOW());
