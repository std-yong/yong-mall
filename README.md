# 🛍️ Shopping Mall API

Spring Boot 기반 의류 쇼핑몰 REST API 서버

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| ORM | Spring Data JPA / Hibernate |
| DB | MySQL 8.0 |
| 인증 | Spring Security + JWT |
| API 문서 | Swagger (SpringDoc OpenAPI) |
| 빌드 | Gradle |

---

## 주요 기능

- **회원** — 회원가입, 로그인, 내 정보 조회
- **상품** — 카테고리별 조회, 키워드 검색, 페이지네이션
- **장바구니** — 상품 담기/수량 변경/삭제, 중복 담기 시 수량 자동 증가
- **주문** — 장바구니 기반 주문 생성, 주문 조회, 취소 (PENDING 상태만)
- **인증/인가** — JWT 토큰 기반 인증, ADMIN/USER 역할 분리

---

## 패키지 구조

```
src/main/java/shop/example/shop/
├── domain/
│   ├── member/       # 회원
│   ├── item/         # 상품, 카테고리
│   ├── cart/         # 장바구니
│   └── order/        # 주문
├── global/
│   ├── jwt/          # JWT 필터, 유틸
│   └── exception/    # 전역 예외 처리
└── config/           # Security, Swagger 설정
```

---

## API 명세

| 메서드 | URL | 권한 | 설명 |
|---|---|---|---|
| POST | /api/auth/signup | - | 회원가입 |
| POST | /api/auth/login | - | 로그인 |
| GET | /api/members/me | USER | 내 정보 조회 |
| GET | /api/categories | - | 카테고리 목록 |
| POST | /api/categories | ADMIN | 카테고리 생성 |
| GET | /api/items | - | 상품 목록 (페이지네이션) |
| GET | /api/items/search | - | 상품 검색 |
| GET | /api/items/{id} | - | 상품 상세 |
| POST | /api/items | ADMIN | 상품 등록 |
| PUT | /api/items/{id} | ADMIN | 상품 수정 |
| DELETE | /api/items/{id} | ADMIN | 상품 삭제 |
| GET | /api/cart | USER | 장바구니 조회 |
| POST | /api/cart/items | USER | 장바구니 담기 |
| PATCH | /api/cart/items/{itemOptionId} | USER | 수량 변경 |
| DELETE | /api/cart/items/{itemOptionId} | USER | 장바구니 삭제 |
| POST | /api/orders | USER | 주문 생성 |
| GET | /api/orders | USER | 주문 목록 |
| GET | /api/orders/{id} | USER | 주문 상세 |
| DELETE | /api/orders/{id} | USER | 주문 취소 |

---

## 트러블슈팅

**JWT role 정보 누락으로 ADMIN 403 발생**
- 원인: `JwtAuthenticationFilter`에서 권한을 항상 `ROLE_USER`로 고정
- 해결: 토큰 생성 시 role 포함, 필터에서 토큰의 role로 권한 설정

**상품 등록 후 options 빈 배열 반환**
- 원인: JPA 1차 캐시 — 같은 트랜잭션 내 재조회 시 캐시된 객체 반환
- 해결: 저장한 옵션 목록을 직접 수집하여 DTO에 전달

**Spring Security URL 패턴 미매칭**
- 원인: `/api/items/**` 패턴이 `/api/items` 자체는 매칭하지 않음
- 해결: 경로 자체와 하위 경로를 모두 명시적으로 등록

---

## 실행 방법

```bash
# 1. application-local.properties 생성
# shop/src/main/resources/application-local.properties
spring.datasource.password=your_db_password
jwt.secret=your_jwt_secret

# 2. 실행
./gradlew bootRun

# 3. Swagger UI
http://localhost:8080/swagger-ui.html
```