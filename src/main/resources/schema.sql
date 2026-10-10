-- Kinetic database schema, executed at start-up. Every statement is idempotent.

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(190) NOT NULL UNIQUE,
    display_name  VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    provider      VARCHAR(20)  NOT NULL,
    google_sub    VARCHAR(64) UNIQUE,
    avatar_url    VARCHAR(500),
    weight_kg     DOUBLE       DEFAULT 70 NOT NULL,
    height_cm     INT          DEFAULT 170 NOT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS activities (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    type         VARCHAR(20) NOT NULL,
    performed_on DATE        NOT NULL,
    duration_min INT         NOT NULL,
    distance_km  DOUBLE      DEFAULT 0 NOT NULL,
    intensity    INT         NOT NULL,
    calories     INT         NOT NULL,
    notes        VARCHAR(500),
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_activity_user_date ON activities (user_id, performed_on);

CREATE TABLE IF NOT EXISTS goals (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT      NOT NULL,
    metric  VARCHAR(20) NOT NULL,
    target  DOUBLE      NOT NULL,
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_goal_metric UNIQUE (user_id, metric)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT,
    event      VARCHAR(40)  NOT NULL,
    detail     VARCHAR(255),
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS daily_logs (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    log_date  DATE   NOT NULL,
    steps     INT    DEFAULT 0 NOT NULL,
    water_ml  INT    DEFAULT 0 NOT NULL,
    sleep_min INT    DEFAULT 0 NOT NULL,
    CONSTRAINT fk_daily_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_daily_day UNIQUE (user_id, log_date)
);

CREATE TABLE IF NOT EXISTS body_metrics (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    measured_on DATE   NOT NULL,
    weight_kg   DOUBLE NOT NULL,
    CONSTRAINT fk_body_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_body_day UNIQUE (user_id, measured_on)
);
