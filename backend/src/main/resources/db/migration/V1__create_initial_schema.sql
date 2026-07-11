CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(120) NOT NULL,
    password VARCHAR(255) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    turns INT NOT NULL DEFAULT 5,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_score CHECK (score >= 0),
    CONSTRAINT chk_users_turns CHECK (turns >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE guess_history (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    guessed_number TINYINT NOT NULL,
    server_number TINYINT NOT NULL,
    result VARCHAR(20) NOT NULL,
    score_after INT NOT NULL,
    turns_after INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_guess_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_guess_history_guessed_number CHECK (guessed_number BETWEEN 1 AND 5),
    CONSTRAINT chk_guess_history_server_number CHECK (server_number BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_history (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    turns_added INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    provider VARCHAR(30) NOT NULL DEFAULT 'DEMO',
    transaction_code VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_purchase_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_score_created ON users(score DESC, created_at ASC, id ASC);
CREATE INDEX idx_guess_history_user_created ON guess_history(user_id, created_at DESC);
CREATE INDEX idx_purchase_history_user_created ON purchase_history(user_id, created_at DESC);
