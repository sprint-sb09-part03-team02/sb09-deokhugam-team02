ALTER TABLE reviews
    DROP CONSTRAINT IF EXISTS uk_book_user_is_deleted;

ALTER TABLE reviews
    DROP CONSTRAINT IF EXISTS uq_reviews_book_user_is_deleted;

ALTER TABLE reviews
    DROP CONSTRAINT IF EXISTS uq_reviews_book_user;

CREATE UNIQUE INDEX IF NOT EXISTS ux_reviews_active_book_user
    ON reviews (book_id, user_id)
    WHERE is_deleted = FALSE;
