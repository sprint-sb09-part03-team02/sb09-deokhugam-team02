CREATE OR REPLACE FUNCTION deokhugam_title_sort_key(input_title TEXT)
RETURNS TEXT AS $$
DECLARE
    source_text TEXT := regexp_replace(
        regexp_replace(lower(btrim(coalesce(input_title, ''))), '[[:punct:]]+', ' ', 'g'),
        '[[:space:]]+',
        ' ',
        'g'
    );
    result_text TEXT := '';
    digit_buffer TEXT := '';
    current_char TEXT;
    index_value INTEGER;
BEGIN
    FOR index_value IN 1..char_length(source_text) LOOP
        current_char := substr(source_text, index_value, 1);

        IF current_char ~ '[0-9]' THEN
            digit_buffer := digit_buffer || current_char;
        ELSE
            IF digit_buffer <> '' THEN
                digit_buffer := regexp_replace(digit_buffer, '^0+(?!$)', '');
                IF char_length(digit_buffer) < 20 THEN
                    result_text := result_text || lpad(digit_buffer, 20, '0');
                ELSE
                    result_text := result_text || digit_buffer;
                END IF;
                digit_buffer := '';
            END IF;

            result_text := result_text || current_char;
        END IF;
    END LOOP;

    IF digit_buffer <> '' THEN
        digit_buffer := regexp_replace(digit_buffer, '^0+(?!$)', '');
        IF char_length(digit_buffer) < 20 THEN
            result_text := result_text || lpad(digit_buffer, 20, '0');
        ELSE
            result_text := result_text || digit_buffer;
        END IF;
    END IF;

    RETURN btrim(result_text);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

ALTER TABLE books
    ADD COLUMN IF NOT EXISTS title_sort_key VARCHAR(500);

UPDATE books
SET title_sort_key = deokhugam_title_sort_key(title)
WHERE title_sort_key IS NULL OR title_sort_key = '';

ALTER TABLE books
    ALTER COLUMN title_sort_key SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_books_title_sort_key_created_at
    ON books (title_sort_key, created_at DESC);

DROP FUNCTION IF EXISTS deokhugam_title_sort_key(TEXT);
