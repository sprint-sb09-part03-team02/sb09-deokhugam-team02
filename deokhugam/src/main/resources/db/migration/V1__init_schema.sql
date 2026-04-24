CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email       VARCHAR(255) NOT NULL,
    nickname    VARCHAR(255) NOT NULL,
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
    isbn            VARCHAR(20) NOT NULL,
    publisher       VARCHAR(255),
    description     TEXT,
    thumbnail_url   VARCHAR(1000),
    published_date  DATE NOT NULL,
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
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id         UUID NOT NULL,
    user_id         UUID NOT NULL,
    content         TEXT NOT NULL,
    rating          INTEGER NOT NULL,
    like_count      INTEGER NOT NULL DEFAULT 0,
    comment_count   INTEGER NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_book_user_is_deleted UNIQUE (book_id, user_id, is_deleted),
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


CREATE TABLE comments (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id   UUID NOT NULL,
    user_id     UUID NOT NULL,
    content     TEXT NOT NULL,
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_comments_review FOREIGN KEY (review_id) REFERENCES reviews (id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_comments_review_created_at ON comments (review_id, created_at ASC);
CREATE INDEX idx_comments_user_created_at ON comments (user_id, created_at DESC);
CREATE INDEX idx_comments_active_cursor ON comments (created_at DESC, id DESC) WHERE is_deleted = FALSE;


CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id   UUID NOT NULL,
    user_id     UUID NOT NULL,
    type        VARCHAR(255) NOT NULL,
    content     TEXT NOT NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted  BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notifications_review FOREIGN KEY (review_id) REFERENCES reviews (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_notifications_user_created_at ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_active_unread ON notifications (user_id, created_at DESC)
    WHERE is_deleted = FALSE AND is_read = FALSE;


CREATE TABLE popular_books (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id          UUID NOT NULL,
    period_type      VARCHAR(20) NOT NULL,
    score            DOUBLE PRECISION NOT NULL,
    rank_order       INTEGER NOT NULL,
    calculated_date  DATE NOT NULL,
    review_count     BIGINT,
    rating           DOUBLE PRECISION NOT NULL DEFAULT 0,
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at       TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_popular_books_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT chk_popular_books_period CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    CONSTRAINT chk_popular_books_review_count CHECK (review_count IS NULL OR review_count >= 0),
    CONSTRAINT chk_popular_books_rating CHECK (rating >= 0 AND rating <= 5)
);

CREATE INDEX idx_popular_books_period_created_at ON popular_books (period_type, created_at DESC);
CREATE INDEX idx_popular_books_active_cursor ON popular_books (created_at DESC, id DESC) WHERE is_deleted = FALSE;


CREATE TABLE popular_reviews (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id        UUID NOT NULL,
    period_type      VARCHAR(20) NOT NULL,
    score            DOUBLE PRECISION NOT NULL,
    rank_order       INTEGER NOT NULL,
    calculated_date  DATE NOT NULL,
    like_count       BIGINT,
    comment_count    BIGINT,
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at       TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_popular_reviews_review FOREIGN KEY (review_id) REFERENCES reviews (id),
    CONSTRAINT chk_popular_reviews_period CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    CONSTRAINT chk_popular_reviews_like_count CHECK (like_count IS NULL OR like_count >= 0),
    CONSTRAINT chk_popular_reviews_comment_count CHECK (comment_count IS NULL OR comment_count >= 0)
);

CREATE INDEX idx_popular_reviews_period_created_at ON popular_reviews (period_type, created_at DESC);
CREATE INDEX idx_popular_reviews_active_cursor ON popular_reviews (created_at DESC, id DESC) WHERE is_deleted = FALSE;


CREATE TABLE power_users (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           UUID NOT NULL,
    period_type       VARCHAR(20) NOT NULL,
    score             DOUBLE PRECISION NOT NULL,
    review_score_sum  DOUBLE PRECISION,
    rank_order        INTEGER NOT NULL,
    calculated_date   DATE NOT NULL,
    like_count        BIGINT,
    comment_count     BIGINT,
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_power_users_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_power_users_period CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    CONSTRAINT chk_power_users_like_count CHECK (like_count IS NULL OR like_count >= 0),
    CONSTRAINT chk_power_users_comment_count CHECK (comment_count IS NULL OR comment_count >= 0)
);

CREATE INDEX idx_power_users_period_created_at ON power_users (period_type, created_at DESC);
CREATE INDEX idx_power_users_active_cursor ON power_users (created_at DESC, id DESC) WHERE is_deleted = FALSE;
