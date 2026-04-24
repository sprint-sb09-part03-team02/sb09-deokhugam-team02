DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'notifications'
          AND column_name = 'message'
    ) THEN
        ALTER TABLE notifications RENAME COLUMN message TO content;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'notifications'
          AND column_name = 'confirmed'
    ) THEN
        ALTER TABLE notifications RENAME COLUMN confirmed TO is_read;
    END IF;
END $$;

ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS type VARCHAR(255);

UPDATE notifications
SET type = 'REVIEW_COMMENTED'
WHERE type IS NULL;

ALTER TABLE notifications
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE notifications
    DROP COLUMN IF EXISTS review_content;


ALTER TABLE reviews
    DROP CONSTRAINT IF EXISTS uq_reviews_book_user;

ALTER TABLE reviews
    DROP CONSTRAINT IF EXISTS uq_reviews_book_user_is_deleted;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_schema = 'public'
          AND table_name = 'reviews'
          AND constraint_name = 'uk_book_user_is_deleted'
    ) THEN
        ALTER TABLE reviews
            ADD CONSTRAINT uk_book_user_is_deleted UNIQUE (book_id, user_id, is_deleted);
    END IF;
END $$;

ALTER TABLE reviews
    DROP COLUMN IF EXISTS liked_by_me_default;


DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'popular_books'
          AND column_name = 'period'
    ) THEN
        ALTER TABLE popular_books RENAME COLUMN period TO period_type;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'popular_books'
          AND column_name = 'rank'
    ) THEN
        ALTER TABLE popular_books RENAME COLUMN rank TO rank_order;
    END IF;
END $$;

ALTER TABLE popular_books
    ADD COLUMN IF NOT EXISTS calculated_date DATE;

UPDATE popular_books
SET calculated_date = created_at::date
WHERE calculated_date IS NULL;

ALTER TABLE popular_books
    ALTER COLUMN calculated_date SET NOT NULL;


DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'popular_reviews'
          AND column_name = 'period'
    ) THEN
        ALTER TABLE popular_reviews RENAME COLUMN period TO period_type;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'popular_reviews'
          AND column_name = 'rank'
    ) THEN
        ALTER TABLE popular_reviews RENAME COLUMN rank TO rank_order;
    END IF;
END $$;

ALTER TABLE popular_reviews
    ADD COLUMN IF NOT EXISTS calculated_date DATE;

UPDATE popular_reviews
SET calculated_date = created_at::date
WHERE calculated_date IS NULL;

ALTER TABLE popular_reviews
    ALTER COLUMN calculated_date SET NOT NULL;

ALTER TABLE popular_reviews
    DROP COLUMN IF EXISTS book_id,
    DROP COLUMN IF EXISTS user_id,
    DROP COLUMN IF EXISTS user_nickname,
    DROP COLUMN IF EXISTS review_content,
    DROP COLUMN IF EXISTS review_rating;


DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'power_users'
          AND column_name = 'period'
    ) THEN
        ALTER TABLE power_users RENAME COLUMN period TO period_type;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'power_users'
          AND column_name = 'rank'
    ) THEN
        ALTER TABLE power_users RENAME COLUMN rank TO rank_order;
    END IF;
END $$;

ALTER TABLE power_users
    ADD COLUMN IF NOT EXISTS calculated_date DATE;

UPDATE power_users
SET calculated_date = created_at::date
WHERE calculated_date IS NULL;

ALTER TABLE power_users
    ALTER COLUMN calculated_date SET NOT NULL;


ALTER TABLE comments
    DROP COLUMN IF EXISTS user_nickname;
