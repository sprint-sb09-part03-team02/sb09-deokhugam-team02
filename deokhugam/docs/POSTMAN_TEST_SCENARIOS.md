# Postman Test Scenarios

## Environment Variables

Create an environment for the deployed API and add these variables.

| Key | Example |
| --- | --- |
| `baseUrl` | `http://<PUBLIC_IP>:8080` |
| `email` | `postman{{$timestamp}}@example.com` |
| `password` | `Password1!` |
| `nickname` | `tester{{$timestamp}}` |
| `updatedNickname` | `tester-updated{{$timestamp}}` |
| `userId` | empty |
| `authUserId` | empty |
| `authToken` | empty |
| `bookId` | empty |
| `reviewId` | empty |
| `commentId` | empty |
| `notificationId` | empty |

## Collection Pre-request Script

Add this to the collection-level `Pre-request Script`.

```javascript
const userId = pm.environment.get("authUserId");
const token = pm.environment.get("authToken");

if (userId) {
  pm.request.headers.upsert({
    key: "Deokhugam-Request-User-ID",
    value: userId
  });
}

if (token) {
  pm.request.headers.upsert({
    key: "Authorization",
    value: token
  });
}
```

## Common Test Helpers

Paste this at the top of each request's `Tests` tab when needed.

```javascript
function json() {
  try {
    return pm.response.json();
  } catch (e) {
    return {};
  }
}

function expectStatus(codes) {
  pm.test(`status is one of ${codes.join(", ")}`, function () {
    pm.expect(codes).to.include(pm.response.code);
  });
}

function bodyData() {
  const body = json();
  return body.data || body;
}
```

## 1. Health Check

`GET {{baseUrl}}/actuator/health`

```javascript
expectStatus([200]);

pm.test("health is UP", function () {
  pm.expect(json().status).to.eql("UP");
});
```

## 2. User Flow

### 2.1 Register

`POST {{baseUrl}}/api/users`

Body:

```json
{
  "email": "{{email}}",
  "nickname": "{{nickname}}",
  "password": "{{password}}"
}
```

Tests:

```javascript
expectStatus([201]);

const data = bodyData();
pm.test("user is created", function () {
  pm.expect(data.id).to.be.a("string");
  pm.expect(data.email).to.eql(pm.environment.get("email"));
  pm.expect(data.nickname).to.eql(pm.environment.get("nickname"));
});

pm.environment.set("userId", data.id);
pm.environment.set("authUserId", data.id);
```

### 2.2 Login

`POST {{baseUrl}}/api/users/login`

Body:

```json
{
  "email": "{{email}}",
  "password": "{{password}}"
}
```

Tests:

```javascript
expectStatus([200]);

const data = bodyData();
const requestUserId = pm.response.headers.get("Deokhugam-Request-User-ID");
const authorization = pm.response.headers.get("Authorization");

pm.test("login returns user and auth headers", function () {
  pm.expect(data.id).to.eql(pm.environment.get("userId"));
  pm.expect(requestUserId).to.eql(data.id);
  pm.expect(authorization).to.match(/^Bearer\s.+/);
});

pm.environment.set("authUserId", requestUserId);
pm.environment.set("authToken", authorization);
```

### 2.3 Get User

`GET {{baseUrl}}/api/users/{{userId}}`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("user detail matches environment user", function () {
  pm.expect(data.id).to.eql(pm.environment.get("userId"));
});
```

### 2.4 Update User

`PATCH {{baseUrl}}/api/users/{{userId}}`

Body:

```json
{
  "nickname": "{{updatedNickname}}"
}
```

Tests:

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("nickname is updated", function () {
  pm.expect(data.nickname).to.eql(pm.environment.get("updatedNickname"));
});
```

### 2.5 Power Users

`GET {{baseUrl}}/api/users/power?period=DAILY&direction=ASC&limit=10`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("power user page shape is valid", function () {
  pm.expect(data.content || data.items || []).to.be.an("array");
});
```

## 3. Book Flow

### 3.1 Create Book

`POST {{baseUrl}}/api/books`

Use `form-data`.

| Key | Type | Value |
| --- | --- | --- |
| `title` | Text | `Postman Book {{$timestamp}}` |
| `author` | Text | `Postman Author` |
| `isbn` | Text | `ISBN{{$timestamp}}` |
| `publisher` | Text | `Postman Publisher` |
| `description` | Text | `Postman test book` |
| `publishedDate` | Text | `2024-01-01` |
| `thumbnailImage` | File | optional |

Tests:

```javascript
expectStatus([201]);

const data = bodyData();
pm.test("book is created", function () {
  pm.expect(data.id).to.be.a("string");
  pm.expect(data.title).to.include("Postman Book");
});

pm.environment.set("bookId", data.id);
```

### 3.2 List Books

`GET {{baseUrl}}/api/books?keyword=Postman&orderBy=title&direction=DESC&limit=10`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("book page shape is valid", function () {
  pm.expect(data.content || data.items || []).to.be.an("array");
});
```

### 3.2.1 List Books - Title Sort ASC

`GET {{baseUrl}}/api/books?orderBy=title&direction=ASC&limit=20`

한글, 영문, 숫자가 섞인 제목 정렬은 서버의 `title_sort_key` 기준으로 처리합니다. 운영 DB에서 `V6__rebuild_book_title_sort_key.sql`이 적용된 뒤에는 한글 제목도 가나다순으로 내려와야 합니다.

```javascript
expectStatus([200]);

const data = bodyData();
const books = data.content || data.items || [];

pm.test("book page shape is valid", function () {
  pm.expect(books).to.be.an("array");
});

pm.test("title ASC order is stable", function () {
  const toTitleSortKey = (title) => {
    const normalized = title
      .trim()
      .normalize("NFKC")
      .toLowerCase()
      .replace(/[\p{Punctuation}]+/gu, " ")
      .replace(/\s+/g, " ")
      .trim()
      .replace(/\d+/g, (value) => {
        const normalizedNumber = value.replace(/^0+(?!$)/, "");
        return normalizedNumber.length >= 20
          ? normalizedNumber
          : normalizedNumber.padStart(20, "0");
      });

    return Array.from(normalized).map((char) => {
      const code = char.codePointAt(0);
      if (code >= 0xAC00 && code <= 0xD7A3) {
        const syllableIndex = code - 0xAC00;
        const choseong = Math.floor(syllableIndex / (21 * 28));
        const jungseong = Math.floor((syllableIndex % (21 * 28)) / 28);
        const jongseong = syllableIndex % 28;
        return `k${String(choseong).padStart(2, "0")}${String(jungseong).padStart(2, "0")}${String(jongseong).padStart(2, "0")}`;
      }
      return char;
    }).join("");
  };

  const titles = books.map((book) => book.title);
  const sorted = [...titles].sort((a, b) => toTitleSortKey(a).localeCompare(toTitleSortKey(b)));

  pm.expect(titles).to.eql(sorted);
});
```

### 3.2.2 List Books - Title Sort DESC

`GET {{baseUrl}}/api/books?orderBy=title&direction=DESC&limit=20`

```javascript
expectStatus([200]);

const data = bodyData();
const books = data.content || data.items || [];

pm.test("book page shape is valid", function () {
  pm.expect(books).to.be.an("array");
});

pm.test("title DESC order is stable", function () {
  const toTitleSortKey = (title) => {
    const normalized = title
      .trim()
      .normalize("NFKC")
      .toLowerCase()
      .replace(/[\p{Punctuation}]+/gu, " ")
      .replace(/\s+/g, " ")
      .trim()
      .replace(/\d+/g, (value) => {
        const normalizedNumber = value.replace(/^0+(?!$)/, "");
        return normalizedNumber.length >= 20
          ? normalizedNumber
          : normalizedNumber.padStart(20, "0");
      });

    return Array.from(normalized).map((char) => {
      const code = char.codePointAt(0);
      if (code >= 0xAC00 && code <= 0xD7A3) {
        const syllableIndex = code - 0xAC00;
        const choseong = Math.floor(syllableIndex / (21 * 28));
        const jungseong = Math.floor((syllableIndex % (21 * 28)) / 28);
        const jongseong = syllableIndex % 28;
        return `k${String(choseong).padStart(2, "0")}${String(jungseong).padStart(2, "0")}${String(jongseong).padStart(2, "0")}`;
      }
      return char;
    }).join("");
  };

  const titles = books.map((book) => book.title);
  const sorted = [...titles].sort((a, b) => toTitleSortKey(b).localeCompare(toTitleSortKey(a)));

  pm.expect(titles).to.eql(sorted);
});
```

### 3.3 Get Book

`GET {{baseUrl}}/api/books/{{bookId}}`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("book detail matches created book", function () {
  pm.expect(data.id).to.eql(pm.environment.get("bookId"));
});
```

### 3.4 Update Book

`PATCH {{baseUrl}}/api/books/{{bookId}}`

Use `form-data`.

| Key | Type | Value |
| --- | --- | --- |
| `title` | Text | `Updated Postman Book` |
| `author` | Text | `Updated Author` |
| `publisher` | Text | `Updated Publisher` |
| `description` | Text | `Updated description` |
| `publishedDate` | Text | `2024-02-01` |
| `thumbnailImage` | File | optional |

Tests:

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("book is updated", function () {
  pm.expect(data.id).to.eql(pm.environment.get("bookId"));
  pm.expect(data.title).to.eql("Updated Postman Book");
});
```

### 3.5 Popular Books

`GET {{baseUrl}}/api/books/popular?period=DAILY&direction=ASC&limit=10`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("popular book page shape is valid", function () {
  pm.expect(data.content || data.items || []).to.be.an("array");
});
```

## 4. Review Flow

### 4.1 Create Review

`POST {{baseUrl}}/api/reviews`

Body:

```json
{
  "bookId": "{{bookId}}",
  "userId": "{{userId}}",
  "content": "Postman review content",
  "rating": 5
}
```

Tests:

```javascript
expectStatus([201]);

const data = bodyData();
pm.test("review is created", function () {
  pm.expect(data.id).to.be.a("string");
  pm.expect(data.bookId).to.eql(pm.environment.get("bookId"));
  pm.expect(data.userId).to.eql(pm.environment.get("userId"));
});

pm.environment.set("reviewId", data.id);
```

### 4.2 List Reviews

`GET {{baseUrl}}/api/reviews?bookId={{bookId}}&orderBy=createdAt&direction=DESC&limit=10`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("review page shape is valid", function () {
  pm.expect(data.content || data.items || []).to.be.an("array");
});
```

### 4.3 Get Review

`GET {{baseUrl}}/api/reviews/{{reviewId}}`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("review detail matches created review", function () {
  pm.expect(data.id).to.eql(pm.environment.get("reviewId"));
});
```

### 4.4 Like Review

`POST {{baseUrl}}/api/reviews/{{reviewId}}/like`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("review like response has review id", function () {
  pm.expect(data.reviewId || pm.environment.get("reviewId")).to.be.a("string");
});
```

### 4.5 Update Review

`PATCH {{baseUrl}}/api/reviews/{{reviewId}}`

Body:

```json
{
  "content": "Updated Postman review content",
  "rating": 4
}
```

Tests:

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("review is updated", function () {
  pm.expect(data.id).to.eql(pm.environment.get("reviewId"));
  pm.expect(data.rating).to.eql(4);
});
```

### 4.6 Popular Reviews

`GET {{baseUrl}}/api/reviews/popular?period=DAILY&direction=DESC&limit=10`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("popular review page shape is valid", function () {
  pm.expect(data.content || data.items || []).to.be.an("array");
});
```

## 5. Comment Flow

### 5.1 Create Comment

`POST {{baseUrl}}/api/comments`

Body:

```json
{
  "reviewId": "{{reviewId}}",
  "userId": "{{userId}}",
  "content": "Postman comment content"
}
```

Tests:

```javascript
expectStatus([201]);

const data = bodyData();
pm.test("comment is created", function () {
  pm.expect(data.id).to.be.a("string");
  pm.expect(data.reviewId).to.eql(pm.environment.get("reviewId"));
});

pm.environment.set("commentId", data.id);
```

### 5.2 List Comments

`GET {{baseUrl}}/api/comments?reviewId={{reviewId}}&direction=DESC&limit=10`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("comment page shape is valid", function () {
  pm.expect(data.content || data.items || []).to.be.an("array");
});
```

### 5.3 Get Comment

`GET {{baseUrl}}/api/comments/{{commentId}}`

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("comment detail matches created comment", function () {
  pm.expect(data.id).to.eql(pm.environment.get("commentId"));
});
```

### 5.4 Update Comment

`PATCH {{baseUrl}}/api/comments/{{commentId}}`

Body:

```json
{
  "content": "Updated Postman comment content"
}
```

Tests:

```javascript
expectStatus([200]);

const data = bodyData();
pm.test("comment is updated", function () {
  pm.expect(data.id).to.eql(pm.environment.get("commentId"));
  pm.expect(data.content).to.eql("Updated Postman comment content");
});
```

## 6. Notification Flow

Notifications are created by domain events such as comments. If no notification is created, list requests can still validate the endpoint shape.

### 6.1 List Notifications

`GET {{baseUrl}}/api/notifications?userId={{userId}}&direction=DESC&limit=10`

```javascript
expectStatus([200]);

const data = bodyData();
const list = data.content || data.items || [];

pm.test("notification page shape is valid", function () {
  pm.expect(list).to.be.an("array");
});

if (list.length > 0) {
  pm.environment.set("notificationId", list[0].id);
}
```

### 6.2 Read Notification

`PATCH {{baseUrl}}/api/notifications/{{notificationId}}`

Body:

```json
{
  "confirmed": true
}
```

Tests:

```javascript
expectStatus([200, 404]);

if (pm.response.code === 200) {
  const data = bodyData();
  pm.test("notification is read", function () {
    pm.expect(data.id).to.eql(pm.environment.get("notificationId"));
  });
}
```

### 6.3 Read All Notifications

`PATCH {{baseUrl}}/api/notifications/read-all`

```javascript
expectStatus([204]);
```

## 7. Negative Test Scenarios

### 7.1 Duplicate Register

Run `POST /api/users` again with the same body.

```javascript
expectStatus([400, 409]);
```

### 7.2 Login With Wrong Password

`POST {{baseUrl}}/api/users/login`

```json
{
  "email": "{{email}}",
  "password": "Wrong123!"
}
```

Tests:

```javascript
expectStatus([400, 401]);
```

### 7.3 Missing Auth Header

Disable the collection pre-request script temporarily and call:

`GET {{baseUrl}}/api/reviews/{{reviewId}}`

```javascript
expectStatus([403]);
```

### 7.4 Invalid UUID Path Variable

`GET {{baseUrl}}/api/users/not-a-uuid`

```javascript
expectStatus([400]);
```

### 7.5 Invalid Validation Body

`POST {{baseUrl}}/api/reviews`

```json
{
  "bookId": "{{bookId}}",
  "userId": "{{userId}}",
  "content": "",
  "rating": 6
}
```

Tests:

```javascript
expectStatus([400]);
```

## 8. Cleanup

Run cleanup at the end if you want to remove test data. Prefer soft delete first.

### 8.1 Delete Comment

`DELETE {{baseUrl}}/api/comments/{{commentId}}`

```javascript
expectStatus([204]);
```

### 8.2 Delete Review

`DELETE {{baseUrl}}/api/reviews/{{reviewId}}`

```javascript
expectStatus([204]);
```

### 8.3 Delete Book

`DELETE {{baseUrl}}/api/books/{{bookId}}`

```javascript
expectStatus([204]);
```

### 8.4 Delete User

`DELETE {{baseUrl}}/api/users/{{userId}}`

```javascript
expectStatus([204]);
```
