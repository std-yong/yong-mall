# 의류 쇼핑몰 API 명세

Base URL: `/api`  
인증 방식: JWT Bearer Token (`Authorization: Bearer {token}`)  
응답 형식: `application/json`

---

## 권한 표기
- `PUBLIC` — 누구나 접근 가능
- `USER` — 로그인한 회원
- `ADMIN` — 관리자

---

## 1. 인증 (Auth)

### 회원가입
```
POST /api/auth/signup
권한: PUBLIC
```
**Request Body**
```json
{
  "email": "alice@email.com",
  "password": "1234abcd!",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "address": "서울시 강남구"
}
```
**Response** `201 Created`
```json
{
  "id": 1,
  "email": "alice@email.com",
  "name": "홍길동"
}
```

---

### 로그인
```
POST /api/auth/login
권한: PUBLIC
```
**Request Body**
```json
{
  "email": "alice@email.com",
  "password": "1234abcd!"
}
```
**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer"
}
```

---

## 2. 회원 (Member)

### 내 정보 조회
```
GET /api/members/me
권한: USER
```
**Response** `200 OK`
```json
{
  "id": 1,
  "email": "alice@email.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "address": "서울시 강남구",
  "role": "USER",
  "createdAt": "2025-01-01T10:00:00"
}
```

---

### 내 정보 수정
```
PUT /api/members/me
권한: USER
```
**Request Body** (수정할 필드만 포함)
```json
{
  "name": "홍길동",
  "phone": "010-9999-8888",
  "address": "서울시 마포구"
}
```
**Response** `200 OK` — 수정된 회원 정보

---

### 비밀번호 변경
```
PUT /api/members/me/password
권한: USER
```
**Request Body**
```json
{
  "currentPassword": "1234abcd!",
  "newPassword": "newPass123!"
}
```
**Response** `200 OK`

---

### 회원 탈퇴
```
DELETE /api/members/me
권한: USER
```
**Response** `204 No Content`

---

## 3. 카테고리 (Category)

### 카테고리 목록 조회
```
GET /api/categories
권한: PUBLIC
```
**Response** `200 OK`
```json
[
  { "id": 1, "name": "상의" },
  { "id": 2, "name": "하의" },
  { "id": 3, "name": "아우터" },
  { "id": 4, "name": "신발" },
  { "id": 5, "name": "액세서리" }
]
```

---

## 4. 상품 (Item)

### 상품 목록 조회
```
GET /api/items?categoryId={id}&status={status}&page={page}&size={size}
권한: PUBLIC
```
**Query Parameters**
| 파라미터 | 타입 | 설명 | 기본값 |
|---|---|---|---|
| categoryId | Long | 카테고리 필터 | - |
| status | String | SELLING \| SOLD_OUT \| STOP | SELLING |
| page | int | 페이지 번호 (0부터) | 0 |
| size | int | 페이지 크기 | 20 |

**Response** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "categoryId": 1,
      "categoryName": "상의",
      "name": "베이직 화이트 티셔츠",
      "price": 29000,
      "status": "SELLING"
    }
  ],
  "totalElements": 4,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

### 상품 상세 조회
```
GET /api/items/{id}
권한: PUBLIC
```
**Response** `200 OK`
```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "상의",
  "name": "베이직 화이트 티셔츠",
  "description": "기본 핏의 화이트 티셔츠",
  "price": 29000,
  "status": "SELLING",
  "options": [
    { "id": 1, "size": "S", "color": "화이트", "stockQuantity": 50 },
    { "id": 2, "size": "M", "color": "화이트", "stockQuantity": 50 },
    { "id": 3, "size": "L", "color": "화이트", "stockQuantity": 30 }
  ],
  "createdAt": "2025-01-01T10:00:00"
}
```

---

### 상품 등록 (관리자)
```
POST /api/items
권한: ADMIN
```
**Request Body**
```json
{
  "categoryId": 1,
  "name": "베이직 화이트 티셔츠",
  "description": "기본 핏의 화이트 티셔츠",
  "price": 29000
}
```
**Response** `201 Created` — 등록된 상품 정보

---

### 상품 수정 (관리자)
```
PUT /api/items/{id}
권한: ADMIN
```
**Request Body** (수정할 필드만 포함)
```json
{
  "name": "베이직 화이트 티셔츠 (리뉴얼)",
  "price": 32000,
  "status": "SOLD_OUT"
}
```
**Response** `200 OK` — 수정된 상품 정보

---

### 상품 삭제 (관리자)
```
DELETE /api/items/{id}
권한: ADMIN
```
**Response** `204 No Content`

---

## 5. 상품 옵션 (Item Option)

### 옵션 추가 (관리자)
```
POST /api/items/{itemId}/options
권한: ADMIN
```
**Request Body**
```json
{
  "size": "XL",
  "color": "블랙",
  "stockQuantity": 20
}
```
**Response** `201 Created`
```json
{
  "id": 7,
  "itemId": 1,
  "size": "XL",
  "color": "블랙",
  "stockQuantity": 20
}
```

---

### 옵션 수정 (관리자)
```
PUT /api/items/{itemId}/options/{optionId}
권한: ADMIN
```
**Request Body**
```json
{
  "stockQuantity": 100
}
```
**Response** `200 OK`

---

### 옵션 삭제 (관리자)
```
DELETE /api/items/{itemId}/options/{optionId}
권한: ADMIN
```
**Response** `204 No Content`

---

## 6. 장바구니 (Cart)

### 장바구니 조회
```
GET /api/cart
권한: USER
```
**Response** `200 OK`
```json
{
  "cartId": 1,
  "items": [
    {
      "cartItemId": 1,
      "itemOptionId": 2,
      "itemName": "베이직 화이트 티셔츠",
      "size": "M",
      "color": "화이트",
      "price": 29000,
      "quantity": 2,
      "subtotal": 58000
    }
  ],
  "totalPrice": 58000
}
```

---

### 장바구니 상품 추가
```
POST /api/cart/items
권한: USER
```
**Request Body**
```json
{
  "itemOptionId": 2,
  "quantity": 2
}
```
**Response** `201 Created`

> 이미 담긴 옵션이면 수량을 합산

---

### 장바구니 수량 변경
```
PUT /api/cart/items/{cartItemId}
권한: USER
```
**Request Body**
```json
{
  "quantity": 3
}
```
**Response** `200 OK`

---

### 장바구니 상품 삭제
```
DELETE /api/cart/items/{cartItemId}
권한: USER
```
**Response** `204 No Content`

---

## 7. 주문 (Order)

### 주문 생성
```
POST /api/orders
권한: USER
```
**Request Body**
```json
{
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "items": [
    { "itemOptionId": 2, "quantity": 2 },
    { "itemOptionId": 5, "quantity": 1 }
  ]
}
```
> 장바구니에서 바로 주문 시: `fromCart: true` 추가 가능 (장바구니 전체 주문)

**Response** `201 Created`
```json
{
  "orderId": 1,
  "totalPrice": 117000,
  "status": "PENDING",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "createdAt": "2025-06-01T12:00:00"
}
```

---

### 내 주문 목록 조회
```
GET /api/orders?page={page}&size={size}
권한: USER
```
**Response** `200 OK`
```json
{
  "content": [
    {
      "orderId": 1,
      "totalPrice": 117000,
      "status": "PENDING",
      "createdAt": "2025-06-01T12:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 주문 상세 조회
```
GET /api/orders/{id}
권한: USER
```
**Response** `200 OK`
```json
{
  "orderId": 1,
  "totalPrice": 117000,
  "status": "PENDING",
  "deliveryAddress": "서울시 강남구 테헤란로 123",
  "createdAt": "2025-06-01T12:00:00",
  "items": [
    {
      "orderItemId": 1,
      "itemName": "베이직 화이트 티셔츠",
      "size": "M",
      "color": "화이트",
      "quantity": 2,
      "orderPrice": 29000,
      "subtotal": 58000
    }
  ]
}
```

---

### 주문 취소
```
PUT /api/orders/{id}/cancel
권한: USER
```
> PENDING 상태일 때만 취소 가능

**Response** `200 OK`

---

### 주문 상태 변경 (관리자)
```
PUT /api/orders/{id}/status
권한: ADMIN
```
**Request Body**
```json
{
  "status": "SHIPPING"
}
```
**Response** `200 OK`

---

## 공통 에러 응답

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "재고가 부족합니다."
}
```

| HTTP 상태 | 상황 |
|---|---|
| 400 | 잘못된 요청 (유효성 검사 실패, 재고 부족 등) |
| 401 | 인증 실패 (토큰 없음 / 만료) |
| 403 | 권한 없음 (USER가 ADMIN 기능 접근) |
| 404 | 리소스 없음 |
| 409 | 충돌 (이미 가입된 이메일 등) |
| 500 | 서버 에러 |
