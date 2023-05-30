CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(100) NOT NULL ,
    email VARCHAR(320) UNIQUE
);

CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(512) NOT NULL,
    available   BOOLEAN NOT NULL,
    owner       BIGINT  NOT NULL,
    request     BIGINT,
    CONSTRAINT fk_items_to_users FOREIGN KEY (owner) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    description VARCHAR(1000),
    available   BOOLEAN                     NOT NULL,
    requestor   BIGINT                      NOT NULL,
    create_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_req_to_users FOREIGN KEY (requestor) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    start_booking TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_booking   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    description   VARCHAR(1000),
    available     BOOLEAN                     NOT NULL,
    item          BIGINT                      NOT NULL,
    booker        BIGINT                      NOT NULL,
    CONSTRAINT fk_items_to_users FOREIGN KEY (item) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_to_users FOREIGN KEY (booker) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS comments
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    text       VARCHAR,
    evaluation VARCHAR(1) NOT NULL,
    user_id    BIGINT     NOT NULL,
    CONSTRAINT fk_comments_to_users FOREIGN KEY (user_id) REFERENCES users (id)
);

