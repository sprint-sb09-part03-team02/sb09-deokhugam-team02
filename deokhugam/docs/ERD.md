# ERD

기준 파일:
- `deokhugam/src/main/resources/schema.sql`
- `deokhugam/src/main/resources/static/api.json`

```mermaid
erDiagram
    users {
        UUID id PK
        VARCHAR email UK
        VARCHAR nickname UK
        VARCHAR password
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    books {
        UUID id PK
        VARCHAR title
        VARCHAR author
        TEXT description
        VARCHAR publisher
        DATE published_date
        VARCHAR isbn UK
        VARCHAR thumbnail_url
        INTEGER review_count
        DOUBLE rating
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    reviews {
        UUID id PK
        UUID book_id FK
        UUID user_id FK
        TEXT content
        INTEGER rating
        INTEGER like_count
        INTEGER comment_count
        BOOLEAN liked_by_me_default
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    review_likes {
        UUID id PK
        UUID review_id FK
        UUID user_id FK
        BOOLEAN liked
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    comments {
        UUID id PK
        UUID review_id FK
        UUID user_id FK
        VARCHAR user_nickname
        TEXT content
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    notifications {
        UUID id PK
        UUID user_id FK
        UUID review_id FK
        TEXT review_content
        TEXT message
        BOOLEAN confirmed
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    popular_books {
        UUID id PK
        UUID book_id FK
        VARCHAR period
        BIGINT rank
        DOUBLE score
        BIGINT review_count
        DOUBLE rating
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    popular_reviews {
        UUID id PK
        UUID review_id FK
        UUID book_id FK
        UUID user_id FK
        VARCHAR user_nickname
        TEXT review_content
        DOUBLE review_rating
        VARCHAR period
        BIGINT rank
        DOUBLE score
        BIGINT like_count
        BIGINT comment_count
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    power_users {
        UUID id PK
        UUID user_id FK
        VARCHAR period
        BIGINT rank
        DOUBLE score
        DOUBLE review_score_sum
        BIGINT like_count
        BIGINT comment_count
        BOOLEAN is_deleted
        TIMESTAMP deleted_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    users ||--o{ reviews : writes
    books ||--o{ reviews : receives
    reviews ||--o{ review_likes : has
    users ||--o{ review_likes : toggles
    reviews ||--o{ comments : has
    users ||--o{ comments : writes
    users ||--o{ notifications : receives
    reviews ||--o{ notifications : triggers
    books ||--o{ popular_books : ranks
    reviews ||--o{ popular_reviews : ranks
    books ||--o{ popular_reviews : belongs_to
    users ||--o{ popular_reviews : authored_by
    users ||--o{ power_users : ranks
```

## Notes

- `popular_books`, `popular_reviews`, `power_users`는 집계/랭킹 스냅샷 테이블입니다.
- 모든 영속 테이블에 `is_deleted`, `deleted_at`을 포함해 soft delete 규칙을 반영했습니다.
- `NaverBookDto`, OCR 요청 스키마처럼 외부 조회/임시 입력 성격의 스키마는 ERD에서 제외했습니다.
