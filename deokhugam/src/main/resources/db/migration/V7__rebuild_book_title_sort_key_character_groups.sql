CREATE OR REPLACE FUNCTION deokhugam_title_sort_key_v3(input_title TEXT)
RETURNS TEXT AS $$
DECLARE
    source_text TEXT := regexp_replace(
        regexp_replace(lower(btrim(coalesce(input_title, ''))), '[[:punct:]]+', ' ', 'g'),
        '[[:space:]]+',
        ' ',
        'g'
    );
    converted_text TEXT := '';
    numeric_normalized_text TEXT := '';
    digit_buffer TEXT := '';
    current_char TEXT;
    code_point INTEGER;
    syllable_index INTEGER;
    choseong INTEGER;
    jungseong INTEGER;
    jongseong INTEGER;
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
                    numeric_normalized_text := numeric_normalized_text || lpad(digit_buffer, 20, '0');
                ELSE
                    numeric_normalized_text := numeric_normalized_text || digit_buffer;
                END IF;
                digit_buffer := '';
            END IF;

            numeric_normalized_text := numeric_normalized_text || current_char;
        END IF;
    END LOOP;

    IF digit_buffer <> '' THEN
        digit_buffer := regexp_replace(digit_buffer, '^0+(?!$)', '');
        IF char_length(digit_buffer) < 20 THEN
            numeric_normalized_text := numeric_normalized_text || lpad(digit_buffer, 20, '0');
        ELSE
            numeric_normalized_text := numeric_normalized_text || digit_buffer;
        END IF;
    END IF;

    FOR index_value IN 1..char_length(numeric_normalized_text) LOOP
        current_char := substr(numeric_normalized_text, index_value, 1);
        code_point := ascii(current_char);

        IF current_char ~ '[0-9]' THEN
            converted_text := converted_text || '0' || current_char;
        ELSIF current_char ~ '[a-z]' THEN
            converted_text := converted_text || '1' || current_char;
        ELSIF code_point BETWEEN 44032 AND 55203 THEN
            syllable_index := code_point - 44032;
            choseong := syllable_index / (21 * 28);
            jungseong := (syllable_index % (21 * 28)) / 28;
            jongseong := syllable_index % 28;
            converted_text := converted_text
                || '2'
                || lpad(choseong::TEXT, 2, '0')
                || lpad(jungseong::TEXT, 2, '0')
                || lpad(jongseong::TEXT, 2, '0');
        ELSIF current_char ~ '[[:space:]]' THEN
            converted_text := converted_text || '3 ';
        ELSE
            converted_text := converted_text || '9' || current_char;
        END IF;
    END LOOP;

    RETURN btrim(converted_text);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

ALTER TABLE books
    ALTER COLUMN title_sort_key TYPE VARCHAR(2048);

UPDATE books
SET title_sort_key = deokhugam_title_sort_key_v3(title);

DROP INDEX IF EXISTS idx_books_title_sort_key_created_at;

CREATE INDEX IF NOT EXISTS idx_books_title_sort_key_created_at
    ON books (title_sort_key, created_at DESC);

DROP FUNCTION IF EXISTS deokhugam_title_sort_key_v3(TEXT);
