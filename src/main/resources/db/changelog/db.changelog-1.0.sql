--liquibase formatted sql

--changeset paradise:1
CREATE TABLE subscription
(
    id           UUID PRIMARY KEY,
    user_id      UUID                     NOT NULL,
    service_name VARCHAR(255)             NOT NULL,
    status       VARCHAR(32)              NOT NULL,
    price        NUMERIC(10, 2)           NOT NULL,
    currency     VARCHAR(3)               NOT NULL,
    start_date   TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date     TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

--changeset paradise:2
CREATE TABLE subscription_status_history
(
    id              BIGSERIAL PRIMARY KEY,
    subscription_id UUID                     NOT NULL REFERENCES subscription (id) ON DELETE CASCADE,
    old_status      VARCHAR(32),
    new_status      VARCHAR(32)              NOT NULL,
    changed_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    reason          VARCHAR(255)
);

--changeset paradise:3
CREATE TABLE subscription_notification
(
    id              BIGSERIAL PRIMARY KEY,
    subscription_id UUID                     NOT NULL REFERENCES subscription (id) ON DELETE CASCADE,
    planned_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at         TIMESTAMP WITH TIME ZONE,
    status          VARCHAR(32)              NOT NULL
);

--changeset paradise:4
CREATE INDEX idx_subs_user_id ON subscription (user_id);
CREATE INDEX idx_subs_status_date ON subscription (status, end_date);
