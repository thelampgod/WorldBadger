CREATE TABLE IF NOT EXISTS chunks (
    server_id       INTEGER NOT NULL,
    x               INTEGER NOT NULL,
    z               INTEGER NOT NULL,
    isNewerThan1_7 BOOLEAN NOT NULL,

    FOREIGN KEY (server_id) REFERENCES servers(id)
            ON UPDATE CASCADE ON DELETE CASCADE,
    UNIQUE(server_id, x, z)
);