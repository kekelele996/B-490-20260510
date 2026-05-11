CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    nickname VARCHAR(255),
    gender VARCHAR(10),
    birthday DATE,
    avatar VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    balance DOUBLE DEFAULT 0.0
);

CREATE TABLE IF NOT EXISTS counselors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    introduction TEXT,
    expertise VARCHAR(255),
    fee DOUBLE,
    qualification TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    available_time VARCHAR(255),
    total_revenue DOUBLE DEFAULT 0.0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    counselor_id BIGINT NOT NULL,
    appointment_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    feedback TEXT,
    rating INT,
    meeting_link VARCHAR(255),
    counselor_reply TEXT,
    counselor_reply_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (counselor_id) REFERENCES counselors(id)
);

CREATE TABLE IF NOT EXISTS withdrawal_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counselor_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    request_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    note TEXT,
    FOREIGN KEY (counselor_id) REFERENCES counselors(id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    timestamp DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    create_time DATETIME NOT NULL,
    type VARCHAR(20),
    related_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counselor_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    FOREIGN KEY (counselor_id) REFERENCES counselors(id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    create_time DATETIME NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
