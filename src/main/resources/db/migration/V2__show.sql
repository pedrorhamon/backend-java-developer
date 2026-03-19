CREATE TABLE IF NOT EXISTS SHOW
(
    id             VARCHAR(36) PRIMARY KEY,
    id_integration integer NOT NULL UNIQUE,
    name           VARCHAR(265),
    type           VARCHAR(265),
    language       VARCHAR(265),
    status         VARCHAR(265),
    runtime        integer,
    average_runtime integer,
    official_site   VARCHAR(265),
    rating         NUMERIC(5, 2),
    summary        TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);