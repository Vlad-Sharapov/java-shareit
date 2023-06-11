CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    name  VARCHAR(100) NOT NULL,
    email VARCHAR(320) UNIQUE
);

CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(512) NOT NULL,
    available   BOOLEAN      NOT NULL,
    owner       BIGINT       NOT NULL,
    request     BIGINT,
    FOREIGN KEY (owner) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    description VARCHAR(1000),
    requestor   BIGINT                      NOT NULL,
    create_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    FOREIGN KEY (requestor) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    start_booking TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_booking   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item          BIGINT                      NOT NULL,
    booker        BIGINT                      NOT NULL,
    status        VARCHAR(100),
    FOREIGN KEY (item) REFERENCES items (id) ON DELETE CASCADE,
    FOREIGN KEY (booker) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    text      VARCHAR                     NOT NULL,
    author_id BIGINT                      NOT NULL,
    item_id   BIGINT                      NOT NULL,
    created   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items (id)
);

