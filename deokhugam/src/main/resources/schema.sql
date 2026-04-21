CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- users (@Table(name = "users"))
CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    email         VARCHAR(255) NOT NULL,
    nickname      VARCHAR(50)  NOT NULL,
    password      VARCHAR(255) NOT NULL,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email     UNIQUE (email),
    CONSTRAINT uq_users_nickname  UNIQUE (nickname)
);

CREATE INDEX idx_users_email      ON users (email)      WHERE is_deleted = FALSE;
CREATE INDEX idx_users_deleted_at ON users (deleted_at) WHERE is_deleted = TRUE;

-- books (@Table(name = "books"))
CREATE TABLE books (
    id             UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    title          VARCHAR(255) NOT NULL,
    author         VARCHAR(100) NOT NULL,
    isbn           VARCHAR(20)  NOT NULL,
    publisher      VARCHAR(100),
    description    TEXT,
    image_url      VARCHAR(500),
    published_at   DATE         NOT NULL,
    is_deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_books_isbn UNIQUE (isbn)
    -- review_count, rating 제거: Book 엔티티에 해당 필드 없음
);

CREATE INDEX idx_books_title     ON books (title)       WHERE is_deleted = FALSE;
CREATE INDEX idx_books_author    ON books (author)      WHERE is_deleted = FALSE;
CREATE INDEX idx_books_isbn      ON books (isbn)        WHERE is_deleted = FALSE;
CREATE INDEX idx_books_published ON books (published_at) WHERE is_deleted = FALSE;


-- review (@Table(name = "review"))
CREATE TABLE review (
    id            UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id       UUID    NOT NULL REFERENCES books(id),
    user_id       UUID    NOT NULL REFERENCES users(id),
    content       TEXT    NOT NULL,
    rating        INT     NOT NULL,
    like_count    INT     NOT NULL DEFAULT 0,
    comment_count INT     NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_review_book_user UNIQUE (book_id, user_id),   -- uk_book_user
    CONSTRAINT chk_review_rating   CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_review_book_id    ON review (book_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_review_user_id    ON review (user_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_review_rating     ON review (rating,  created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_review_created_at ON review (created_at DESC);          -- 배치용(논리삭제 포함)


-- comments  (@Table(name = "comments"))
CREATE TABLE comments (
    id         UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id  UUID    NOT NULL REFERENCES review(id),
    user_id    UUID    NOT NULL REFERENCES users(id),
    content    TEXT    NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_review_id ON comments (review_id, created_at ASC);


-- review_like  (@Table(name = "review_like"))  ← likes → review_like
CREATE TABLE review_like (
    id         UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id  UUID    NOT NULL REFERENCES review(id),
    user_id    UUID    NOT NULL REFERENCES users(id),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_review_like_review_user UNIQUE (review_id, user_id)
);

CREATE INDEX idx_review_like_user_id   ON review_like (user_id);
CREATE INDEX idx_review_like_review_id ON review_like (review_id);
CREATE INDEX idx_review_like_active ON review_like (review_id, user_id)
    WHERE is_deleted = FALSE;


-- notification  (@Table(name = "notification"))  ← notifications → notification
CREATE TABLE notification (
    id         UUID    PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id  UUID    NOT NULL REFERENCES review(id),
    user_id    UUID    NOT NULL REFERENCES users(id),
    type       VARCHAR(30) NOT NULL,
    content    TEXT    NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_notification_type CHECK (type IN ('REVIEW_LIKED','REVIEW_COMMENTED','REVIEW_RANKED'))
    -- NotificationType 실제 enum 값 반영 (LIKE,COMMENT,RANKING → 수정)
);

CREATE INDEX idx_notification_user_id ON notification (user_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_notification_is_read ON notification (is_read, created_at) WHERE is_read = TRUE;
CREATE INDEX idx_notification_unread_active ON notification (user_id, created_at DESC)
    WHERE is_deleted = FALSE AND is_read = FALSE;


-- popular_book  (클래스명 PopularBook → 기본 테이블명 popular_book)
CREATE TABLE popular_book (
    id              UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id         UUID             NOT NULL REFERENCES books(id),
    period_type     VARCHAR(10)      NOT NULL,
    score           DOUBLE PRECISION NOT NULL,
    rank_order      INT              NOT NULL,
    calculated_date DATE             NOT NULL,
    review_count    BIGINT,
    rating          DOUBLE PRECISION,
    is_deleted      BOOLEAN          NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP        NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_popular_book_period_rank UNIQUE (period_type, calculated_date, rank_order),
    CONSTRAINT chk_popular_book_period     CHECK  (period_type IN ('DAILY','WEEKLY','MONTHLY','ALL_TIME'))
);

CREATE INDEX idx_popular_book_period_date ON popular_book (period_type, calculated_date DESC)
    WHERE is_deleted = FALSE;


-- popular_review  (클래스명 PopularReview → 기본 테이블명 popular_review)
CREATE TABLE popular_review (
    id              UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id       UUID             NOT NULL REFERENCES review(id),
    period_type     VARCHAR(10)      NOT NULL,
    score           DOUBLE PRECISION NOT NULL,
    rank_order      INT              NOT NULL,
    calculated_date DATE             NOT NULL,
    like_count      BIGINT,
    comment_count   BIGINT,
    is_deleted      BOOLEAN          NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP        NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_popular_review_period_rank UNIQUE (period_type, calculated_date, rank_order),
    CONSTRAINT chk_popular_review_period     CHECK  (period_type IN ('DAILY','WEEKLY','MONTHLY','ALL_TIME'))
);

CREATE INDEX idx_popular_review_period_date ON popular_review (period_type, calculated_date DESC)
    WHERE is_deleted = FALSE;


-- power_user  (클래스명 PowerUser → 기본 테이블명 power_user)
CREATE TABLE power_user (
    id                UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           UUID             NOT NULL REFERENCES users(id),
    period_type       VARCHAR(10)      NOT NULL,
    score             DOUBLE PRECISION NOT NULL,
    review_score_sum  DOUBLE PRECISION,
    rank_order        INT              NOT NULL,
    calculated_date   DATE             NOT NULL,
    like_count        INT,
    comment_count     INT,
    is_deleted        BOOLEAN          NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP,
    created_at        TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP        NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_power_user_period_rank UNIQUE (period_type, calculated_date, rank_order),
    CONSTRAINT chk_power_user_period     CHECK  (period_type IN ('DAILY','WEEKLY','MONTHLY','ALL_TIME'))
);

CREATE INDEX idx_power_user_period_date ON power_user (period_type, calculated_date DESC)
    WHERE is_deleted = FALSE;
