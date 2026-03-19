CREATE TABLE episode
(
    id             VARCHAR(36) PRIMARY KEY,
    id_integration INTEGER     NOT NULL,
    fk_show        VARCHAR(36) NOT NULL REFERENCES SHOW (id),
    name           VARCHAR(265),
    season         INTEGER,
    number         INTEGER,
    type           VARCHAR(265),
    airdate        VARCHAR(50),
    airtime        VARCHAR(50),
    airstamp       TIMESTAMPTZ,
    runtime        INTEGER,
    rating         NUMERIC(5, 2),
    summary        TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
