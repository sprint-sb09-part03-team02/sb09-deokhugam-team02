CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- api.json 기준 영속 테이블 정의
-- 외부 조회용 스키마(NaverBookDto, OCR 요청 등)는 저장 테이블에서 제외

CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email       VARCHAR(255) NOT NULL,
    nickname    VARCHAR(20) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_nickname UNIQUE (nickname)
);

CREATE INDEX idx_users_created_at ON users (created_at DESC);
CREATE INDEX idx_users_active_email ON users (email) WHERE is_deleted = FALSE;


CREATE TABLE books (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           VARCHAR(255) NOT NULL,
    author          VARCHAR(255) NOT NULL,
    description     TEXT,
    publisher       VARCHAR(255),
    published_date  DATE NOT NULL,
    isbn            VARCHAR(20) NOT NULL,
    thumbnail_url   VARCHAR(1000),
    review_count    INTEGER NOT NULL DEFAULT 0,
    rating          DOUBLE PRECISION NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_books_isbn UNIQUE (isbn),
    CONSTRAINT chk_books_review_count CHECK (review_count >= 0),
    CONSTRAINT chk_books_rating CHECK (rating >= 0 AND rating <= 5)
);

CREATE INDEX idx_books_title_created_at ON books (title, created_at DESC);
CREATE INDEX idx_books_author_created_at ON books (author, created_at DESC);
CREATE INDEX idx_books_published_date_created_at ON books (published_date DESC, created_at DESC);
CREATE INDEX idx_books_active_isbn ON books (isbn) WHERE is_deleted = FALSE;


CREATE TABLE reviews (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id             UUID NOT NULL,
    user_id             UUID NOT NULL,
    content             TEXT NOT NULL,
    rating              INTEGER NOT NULL,
    like_count          INTEGER NOT NULL DEFAULT 0,
    comment_count       INTEGER NOT NULL DEFAULT 0,
    liked_by_me_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_reviews_book_user UNIQUE (book_id, user_id),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_reviews_like_count CHECK (like_count >= 0),
    CONSTRAINT chk_reviews_comment_count CHECK (comment_count >= 0)
);

CREATE INDEX idx_reviews_book_created_at ON reviews (book_id, created_at DESC);
CREATE INDEX idx_reviews_user_created_at ON reviews (user_id, created_at DESC);
CREATE INDEX idx_reviews_rating_created_at ON reviews (rating DESC, created_at DESC);
CREATE INDEX idx_reviews_active_cursor ON reviews (created_at DESC, id DESC) WHERE is_deleted = FALSE;


CREATE TABLE review_likes (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id   UUID NOT NULL,
    user_id     UUID NOT NULL,
    liked       BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id) REFERENCES reviews (id),
    CONSTRAINT fk_review_likes_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_review_likes_review_user UNIQUE (review_id, user_id)
);

CREATE INDEX idx_review_likes_review_created_at ON review_likes (review_id, created_at DESC);
CREATE INDEX idx_review_likes_user_created_at ON review_likes (user_id, created_at DESC);
CREATE INDEX idx_review_likes_active ON review_likes (review_id, user_id) WHERE is_deleted = FALSE;


CREATE TABLE comments (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id      UUID NOT NULL,
    user_id        UUID NOT NULL,
    user_nickname  VARCHAR(20),
    content        TEXT NOT NULL,
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_comments_review FOREIGN KEY (review_id) REFERENCES reviews (id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_comments_review_created_at ON comments (review_id, created_at ASC);
CREATE INDEX idx_comments_user_created_at ON comments (user_id, created_at DESC);
CREATE INDEX idx_comments_active_cursor ON comments (created_at DESC, id DESC) WHERE is_deleted = FALSE;


CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL,
    review_id       UUID NOT NULL,
    review_content  TEXT,
    message         TEXT NOT NULL,
    confirmed       BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_notifications_review FOREIGN KEY (review_id) REFERENCES reviews (id)
);

CREATE INDEX idx_notifications_user_created_at ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_confirmed_created_at ON notifications (confirmed, created_at DESC);
CREATE INDEX idx_notifications_active_unconfirmed ON notifications (user_id, created_at DESC)
    WHERE is_deleted = FALSE AND confirmed = FALSE;


CREATE TABLE popular_books (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id       UUID NOT NULL,
    period        VARCHAR(20) NOT NULL,
    rank          BIGINT NOT NULL,
    score         DOUBLE PRECISION NOT NULL,
    review_count  BIGINT NOT NULL DEFAULT 0,
    rating        DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted    BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,

    CONSTRAINT fk_popular_books_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT uq_popular_books_period_rank_created_at UNIQUE (period, rank, created_at),
    CONSTRAINT chk_popular_books_period CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    CONSTRAINT chk_popular_books_review_count CHECK (review_count >= 0),
    CONSTRAINT chk_popular_books_rating CHECK (rating >= 0 AND rating <= 5)
);

CREATE INDEX idx_popular_books_period_created_at ON popular_books (period, created_at DESC);
CREATE INDEX idx_popular_books_active_cursor ON popular_books (created_at DESC, id DESC) WHERE is_deleted = FALSE;


CREATE TABLE popular_reviews (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id         UUID NOT NULL,
    book_id           UUID NOT NULL,
    user_id           UUID NOT NULL,
    user_nickname     VARCHAR(20),
    review_content    TEXT,
    review_rating     DOUBLE PRECISION NOT NULL DEFAULT 0,
    period            VARCHAR(20) NOT NULL,
    rank              BIGINT NOT NULL,
    score             DOUBLE PRECISION NOT NULL,
    like_count        BIGINT NOT NULL DEFAULT 0,
    comment_count     BIGINT NOT NULL DEFAULT 0,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP,

    CONSTRAINT fk_popular_reviews_review FOREIGN KEY (review_id) REFERENCES reviews (id),
    CONSTRAINT fk_popular_reviews_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_popular_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_popular_reviews_period_rank_created_at UNIQUE (period, rank, created_at),
    CONSTRAINT chk_popular_reviews_period CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    CONSTRAINT chk_popular_reviews_review_rating CHECK (review_rating >= 0 AND review_rating <= 5),
    CONSTRAINT chk_popular_reviews_like_count CHECK (like_count >= 0),
    CONSTRAINT chk_popular_reviews_comment_count CHECK (comment_count >= 0)
);

CREATE INDEX idx_popular_reviews_period_created_at ON popular_reviews (period, created_at DESC);
CREATE INDEX idx_popular_reviews_active_cursor ON popular_reviews (created_at DESC, id DESC) WHERE is_deleted = FALSE;


CREATE TABLE power_users (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           UUID NOT NULL,
    period            VARCHAR(20) NOT NULL,
    rank              BIGINT NOT NULL,
    score             DOUBLE PRECISION NOT NULL,
    review_score_sum  DOUBLE PRECISION NOT NULL DEFAULT 0,
    like_count        BIGINT NOT NULL DEFAULT 0,
    comment_count     BIGINT NOT NULL DEFAULT 0,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP,

    CONSTRAINT fk_power_users_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_power_users_period_rank_created_at UNIQUE (period, rank, created_at),
    CONSTRAINT chk_power_users_period CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    CONSTRAINT chk_power_users_like_count CHECK (like_count >= 0),
    CONSTRAINT chk_power_users_comment_count CHECK (comment_count >= 0)
);

CREATE INDEX idx_power_users_period_created_at ON power_users (period, created_at DESC);
CREATE INDEX idx_power_users_active_cursor ON power_users (created_at DESC, id DESC) WHERE is_deleted = FALSE;
