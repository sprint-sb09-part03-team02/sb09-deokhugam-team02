ALTER TABLE books
    DROP CONSTRAINT IF EXISTS uq_books_isbn;

ALTER TABLE books
    DROP CONSTRAINT IF EXISTS uk_books_isbn;

CREATE UNIQUE INDEX IF NOT EXISTS ux_books_active_isbn
    ON books (isbn)
    WHERE is_deleted = FALSE;
