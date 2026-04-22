# 의류 쇼핑몰 개발 일지

---

## 목차
1. [프로젝트 설계](#1-프로젝트-설계)
2. [Spring Boot 프로젝트 생성](#2-spring-boot-프로젝트-생성)
3. [MySQL 연결 설정](#3-mysql-연결-설정)
4. [앱 첫 실행](#4-앱-첫-실행)
5. [Security 설정 및 Swagger 확인](#5-security-설정-및-swagger-확인)
6. [패키지 구조 설계 및 Entity 작성](#6-패키지-구조-설계-및-entity-작성)
7. [Repository 작성](#7-repository-작성)
8. [JWT + 회원가입/로그인 API 구현](#8-jwt--회원가입로그인-api-구현)
9. [JWT 인증 흐름 테스트](#9-jwt-인증-흐름-테스트)
10. [GlobalExceptionHandler 구현](#10-globalexceptionhandler-구현)
11. [상품 API 구현](#11-상품-api-구현)

---

## 1. 프로젝트 설계

### 기술 스택
| 분류 | 기술 |
|---|---|
| Backend | Spring Boot 3.5.x |
| ORM | Spring Data JPA |
| 인증 | Spring Security + JWT |
| DB | MySQL 8.x |
| API 문서 | Swagger (SpringDoc OpenAPI) |
| 빌드 | Gradle |
| 프론트 | REST API 분리 방식 (Swagger로 테스트) |

### 생성 파일
- [schema.sql](schema.sql) — MySQL DDL (테이블 생성 쿼리)
- [schema.dbml](schema.dbml) — dbdiagram.io ERD 시각화용
- [api-spec.md](api-spec.md) — API 명세서

### ERD 구조
```
member ──< orders ──< order_item >── item_option >── item >── category
member ──  cart   ──< cart_item  >── item_option
```

### 테이블 목록
| 테이블 | 설명 |
|---|---|
| member | 회원 |
| category | 카테고리 (상의/하의/아우터/신발/액세서리) |
| item | 상품 |
| item_option | 상품 옵션 (사이즈 + 색상 + 재고) |
| cart | 장바구니 (회원당 1개) |
| cart_item | 장바구니 담긴 상품 |
| orders | 주문 |
| order_item | 주문 상품 (가격 스냅샷 포함) |

---

## 2. Spring Boot 프로젝트 생성

### Spring Initializr 설정
| 항목 | 값 |
|---|---|
| Project | Gradle |
| Language | Java |
| Spring Boot | 3.5.x |
| Group | shop.example |
| Artifact | shop |
| Packaging | Jar |
| Java | 21 |

### 추가한 의존성
| 의존성 | 역할 |
|---|---|
| Spring Web | REST API |
| Spring Data JPA | DB ORM |
| MySQL Driver | MySQL 연결 |
| Spring Security | 인증/권한 |
| Lombok | 반복 코드 제거 |
| Validation | 입력값 검증 |
| SpringDoc OpenAPI | Swagger 문서화 |

### 프로젝트 구조
```
shop/
├── build.gradle
├── settings.gradle
├── gradlew
└── src/
    └── main/
        ├── java/shop/example/shop/
        │   └── ShopApplication.java
        └── resources/
            └── application.properties
```

---

## 3. MySQL 연결 설정

### MySQL 정보
- 버전: MySQL 8.0.44
- 설치 경로: `/usr/local/mysql`
- DB명: `shopping_mall`

### 트러블슈팅: MySQL 비밀번호 분실

**증상**
```
ERROR 1045 (28000): Access denied for user 'root'@'localhost'
```

**원인**
MySQL 설치 시 발급된 임시 비밀번호를 잊어버림

**해결 방법**
1. macOS 시스템 설정 → MySQL → Stop MySQL Server
2. 안전 모드로 재시작
```bash
sudo /usr/local/mysql/bin/mysqld_safe --skip-grant-tables --skip-networking &
```
3. 비밀번호 없이 접속 후 변경
```bash
/usr/local/mysql/bin/mysql -u root
```
```sql
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '새비밀번호';
exit;
```
4. 시스템 설정에서 MySQL 다시 Start

### application.properties 설정
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shopping_mall?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

springdoc.swagger-ui.path=/swagger-ui.html
```

> 비밀번호는 보안상 생략

---

## 4. 앱 첫 실행

### 트러블슈팅: Java 21 미설치

**증상**
```
Cannot find a Java installation on your machine matching: {languageVersion=21}
```

**원인**
`build.gradle`에서 Java 21을 요구하는데, 로컬에 Java 25, 17만 설치되어 있었음

**해결 방법**
Java 21 (Temurin) 설치
```bash
brew install --cask temurin@21
```

### 실행 명령어
```bash
cd ~/study-java-with-claude/shop && ./gradlew bootRun
```

### 트러블슈팅: MySQL 안전 모드로 인한 연결 실패

**증상**
```
Communications link failure
Connection refused
```

**원인**
비밀번호 재설정 시 사용한 안전 모드(`--skip-networking`)가 그대로 떠있어서
네트워크 연결이 완전 차단된 상태였음

**해결 방법**
```bash
sudo pkill -f mysqld_safe
sudo pkill -f "mysqld --basedir"
```
이후 시스템 설정에서 MySQL 정상 모드로 재시작

### 첫 실행 성공 확인
```
HikariPool-1 - Added connection  ✅  MySQL 연결 성공
HikariPool-1 - Start completed   ✅  커넥션 풀 정상 시작
Started ShopApplication          ✅  앱 정상 실행
Tomcat started on port 8080      ✅  서버 켜짐
```

---

## 5. Security 설정 및 Swagger 확인

### 트러블슈팅: Swagger 접근 시 로그인 창 / 빈 화면

**증상**
- http://localhost:8080/swagger-ui.html 접속 시 로그인 창 뜸
- 로그인해도 `{"status":999,"error":"None"}` 만 표시됨

**원인**
Spring Security가 기본적으로 모든 경로(Swagger 포함)에 인증을 요구함.
별도 Security 설정이 없으면 자동 생성된 임시 비밀번호로만 접근 가능하고, Swagger 경로도 차단됨.

**해결 방법**
`SecurityConfig.java` 생성하여 Swagger 경로 허용

```java
// src/main/java/shop/example/shop/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
```

> `anyRequest().permitAll()` — 현재는 개발 편의상 전체 허용. JWT 구현 후 수정 예정.

### Swagger UI 접속 확인 ✅
- URL: http://localhost:8080/swagger-ui.html
- 앱이 실행 중일 때만 접근 가능 (로컬에서만 동작하는 내장 페이지)

### 현재 패키지 구조
```
shop/
└── src/main/java/shop/example/shop/
    ├── ShopApplication.java
    └── config/
        └── SecurityConfig.java
```

---

---

## 6. 패키지 구조 설계 및 Entity 작성

### 패키지 구조

역할별로 폴더를 나눠서 코드를 관리. 각 도메인(member, item, cart, order)마다 동일한 구조를 가짐.

```
shop/
└── src/main/java/shop/example/shop/
    ├── ShopApplication.java
    ├── config/
    │   └── SecurityConfig.java
    ├── domain/
    │   ├── member/
    │   │   ├── entity/        Member.java
    │   │   ├── repository/
    │   │   ├── service/
    │   │   ├── controller/
    │   │   └── dto/
    │   ├── item/
    │   │   ├── entity/        Category.java, Item.java, ItemOption.java
    │   │   ├── repository/
    │   │   ├── service/
    │   │   ├── controller/
    │   │   └── dto/
    │   ├── cart/
    │   │   ├── entity/        Cart.java, CartItem.java
    │   │   ├── repository/
    │   │   ├── service/
    │   │   ├── controller/
    │   │   └── dto/
    │   └── order/
    │       ├── entity/        Order.java, OrderItem.java
    │       ├── repository/
    │       ├── service/
    │       ├── controller/
    │       └── dto/
    └── global/
        ├── exception/
        └── response/
```

### 각 패키지 역할

| 패키지 | 역할 |
|---|---|
| `entity` | DB 테이블과 연결되는 클래스 (JPA가 테이블 자동 생성) |
| `repository` | DB 조회/저장 담당 (SQL 없이 Java 메서드로 처리) |
| `service` | 비즈니스 로직 (재고 확인, 가격 계산 등) |
| `controller` | HTTP 요청/응답 처리 (API 엔드포인트) |
| `dto` | 요청/응답 데이터 형식 정의 |

### 작성한 Entity 목록

| 파일 | 주요 어노테이션 | 특징 |
|---|---|---|
| `Member.java` | `@Entity`, `@Builder`, `@Enumerated` | Role enum (USER/ADMIN) |
| `Category.java` | `@Entity` | 단순 구조 |
| `Item.java` | `@ManyToOne`, `@OneToMany` | Category 참조, 양방향 연관관계 |
| `ItemOption.java` | `@ManyToOne`, `@Enumerated` | Size enum, 재고 차감/증가 메서드 포함 |
| `Cart.java` | `@OneToOne`, `orphanRemoval` | 회원 1:1, CartItem 삭제 시 자동 제거 |
| `CartItem.java` | `@ManyToOne` | Cart + ItemOption 참조 |
| `Order.java` | `@ManyToOne`, `@OneToMany` | 취소 시 PENDING 상태 체크 로직 포함 |
| `OrderItem.java` | `@ManyToOne` | orderPrice 스냅샷, subtotal 계산 메서드 |

### 핵심 개념 정리

**연관관계 어노테이션**
- `@ManyToOne` — N:1 관계. FK를 이 쪽이 가짐
- `@OneToMany(mappedBy)` — 1:N 관계. FK는 상대방이 가짐
- `@OneToOne` — 1:1 관계 (Cart ↔ Member)
- `fetch = FetchType.LAZY` — 실제로 필요할 때만 DB 조회 (성능 최적화)

**Entity 설계 원칙**
- 비즈니스 로직을 Entity 안에 넣음 (ex. `decreaseStock()`, `cancel()`)
- 외부에서 `new`로 직접 생성 못하게 `@NoArgsConstructor(PROTECTED)` 사용
- 객체 생성은 `@Builder`로만 허용

### DB 테이블 자동 생성 확인 방법
```bash
# 앱 실행 후
/usr/local/mysql/bin/mysql -u root -p비밀번호 -e "USE shopping_mall; SHOW TABLES;"
```

---

---

## 7. Repository 작성

Repository는 DB 조회/저장을 담당하는 인터페이스. `JpaRepository`를 상속받으면 기본 CRUD가 자동 생성됨.

### 작성한 Repository 목록

| Repository | 추가 메서드 | 용도 |
|---|---|---|
| `MemberRepository` | `findByEmail`, `existsByEmail` | 로그인, 이메일 중복 체크 |
| `CategoryRepository` | 없음 (기본 CRUD만) | 카테고리 목록 조회 |
| `ItemRepository` | `findByCategoryId`, `findByStatus` 등 | 상품 목록 필터링 |
| `ItemOptionRepository` | `findByItemId` | 상품 옵션 조회 |
| `CartRepository` | `findByMemberId` | 회원의 장바구니 조회 |
| `CartItemRepository` | `findByCartIdAndItemOptionId` | 장바구니 중복 옵션 확인 |
| `OrderRepository` | `findByMemberId` | 회원의 주문 목록 |
| `OrderItemRepository` | `findByOrderId` | 주문 상품 목록 |

### 메서드 이름 규칙
JPA는 메서드 이름만으로 SQL을 자동 생성함
```java
findByEmail(String email)
→ SELECT * FROM member WHERE email = ?

findByCategoryIdAndStatus(Long categoryId, Item.Status status, Pageable pageable)
→ SELECT * FROM item WHERE category_id = ? AND status = ? LIMIT ? OFFSET ?
```

---

---

## 8. JWT + 회원가입/로그인 API 구현

### 추가한 파일 목록

```
global/jwt/
    JwtUtil.java                  → 토큰 생성/검증 유틸
    JwtAuthenticationFilter.java  → 매 요청마다 토큰 검증 필터

domain/member/
    dto/
        SignupRequest.java         → 회원가입 요청 DTO
        LoginRequest.java          → 로그인 요청 DTO
        LoginResponse.java         → 로그인 응답 DTO (토큰 포함)
        MemberResponse.java        → 회원 정보 응답 DTO
    service/
        MemberService.java         → 회원가입/로그인 비즈니스 로직
    controller/
        AuthController.java        → POST /api/auth/signup, /login
        MemberController.java      → GET /api/members/me
```

### 전체 호출 흐름

```
[회원가입]
POST /api/auth/signup
    → AuthController
    → MemberService (이메일 중복 확인 → 비밀번호 암호화 → DB 저장)
    → 201 Created + MemberResponse

[로그인]
POST /api/auth/login
    → AuthController
    → MemberService (회원 조회 → 비밀번호 검증 → JWT 발급)
    → 200 OK + LoginResponse { accessToken }

[인증이 필요한 요청]
GET /api/members/me
+ Header: Authorization: Bearer {토큰}
    → JwtAuthenticationFilter (토큰 검증 → 회원 ID 추출)
    → MemberController
    → MemberService
    → 200 OK + MemberResponse
```

### build.gradle에 추가한 의존성
```groovy
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

### application.properties에 추가한 설정
```properties
jwt.secret=shopping-mall-secret-key-please-change-in-production
jwt.expiration=86400000  # 24시간 (밀리초)
```

### SecurityConfig 변경 사항
- 세션 방식 → `STATELESS` (JWT 방식은 세션 불필요)
- `JwtAuthenticationFilter`를 필터 체인에 등록
- `PasswordEncoder` (BCrypt) Bean 등록
- 경로별 권한 설정: `/api/auth/**` 허용, 나머지는 인증 필요

### Swagger 테스트 결과
- `POST /api/auth/signup` → **201 Created** ✅
- DB `member` 테이블에 데이터 저장 확인 ✅
- 비밀번호는 BCrypt로 암호화되어 저장됨

---

## 다음 단계
- [x] 앱 첫 실행 성공 확인
- [x] Security 설정 및 Swagger 확인
- [x] 패키지 구조 설계
- [x] Entity 클래스 작성 (7개)
- [x] DB 테이블 자동 생성 확인
- [x] Repository 인터페이스 작성 (8개)
- [x] JWT 라이브러리 추가
- [x] JwtUtil, JwtAuthenticationFilter 작성
- [x] 회원가입 / 로그인 API 구현
- [x] Swagger로 회원가입 테스트 성공
---

## 9. JWT 인증 흐름 테스트

### 테스트 순서

**1) 회원가입** `POST /api/auth/signup` → 201 Created ✅
```json
{
  "email": "alice@email.com",
  "password": "12345678",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "address": "서울시 강남구"
}
```

**2) 로그인** `POST /api/auth/login` → 200 OK ✅
```json
{ "email": "alice@email.com", "password": "12345678" }
```
응답:
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer"
}
```

**3) Swagger Authorize 설정**
- Swagger 우측 상단 **Authorize 🔒** 버튼 클릭
- 토큰 값만 입력 (`Bearer` 없이)
- → 이후 모든 요청에 `Authorization: Bearer {토큰}` 자동 포함

> ⚠️ 앱 재시작하면 Authorize 초기화됨 → 로그인 후 다시 등록 필요

**4) 내 정보 조회** `GET /api/members/me` → 200 OK ✅
```json
{
  "id": 2,
  "email": "alice@email.com",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "address": "서울시 강남구",
  "role": "USER",
  "createdAt": "2026-04-11T20:26:08"
}
```
- 비밀번호 미포함 확인 ✅ (DTO 덕분)

### 트러블슈팅: Swagger Authorize 버튼 없음

**증상**
Swagger 우측 상단에 Authorize 버튼이 안 보임

**원인**
Swagger에 JWT 인증 방식을 알려주는 설정이 없었음

**해결 방법**
`SwaggerConfig.java` 추가
```java
@Bean
public OpenAPI openAPI() {
    SecurityScheme securityScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

    return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components()
                    .addSecuritySchemes("BearerAuth", securityScheme));
}
```

### 트러블슈팅: 없는 이메일로 로그인 시 403

**증상**
존재하지 않는 이메일로 로그인 → 403 Forbidden

**원인**
GlobalExceptionHandler가 없어서 `IllegalArgumentException`을 Spring Security가 403으로 처리함

**해결**
GlobalExceptionHandler 구현 → 10번 섹션 참고

---

## 10. GlobalExceptionHandler 구현

### 왜 필요한가?

예외를 던져도 받아서 처리하는 핸들러가 없으면 Spring Security가 대신 처리해서 무조건 403이 됨.
`@RestControllerAdvice`로 모든 예외를 한 곳에서 잡아 통일된 에러 응답을 반환.

### 추가한 파일

```
global/exception/
    ErrorCode.java              → 에러 코드 열거형 (HTTP 상태 + 메시지)
    CustomException.java        → 우리가 직접 던지는 커스텀 예외
    ErrorResponse.java          → 에러 응답 DTO
    GlobalExceptionHandler.java → @RestControllerAdvice, 예외 → 통일된 응답 반환
```

### 에러 응답 형식

```json
{
  "code": "MEMBER_NOT_FOUND",
  "message": "존재하지 않는 회원입니다."
}
```

### 정의된 에러 코드

| 코드 | HTTP 상태 | 메시지 |
|---|---|---|
| `DUPLICATE_EMAIL` | 409 Conflict | 이미 사용 중인 이메일입니다. |
| `MEMBER_NOT_FOUND` | 404 Not Found | 존재하지 않는 회원입니다. |
| `INVALID_PASSWORD` | 401 Unauthorized | 비밀번호가 일치하지 않습니다. |
| `CATEGORY_NOT_FOUND` | 404 Not Found | 존재하지 않는 카테고리입니다. |
| `ITEM_NOT_FOUND` | 404 Not Found | 존재하지 않는 상품입니다. |
| `OUT_OF_STOCK` | 400 Bad Request | 재고가 부족합니다. |
| `ORDER_NOT_FOUND` | 404 Not Found | 존재하지 않는 주문입니다. |
| `INVALID_INPUT` | 400 Bad Request | 입력값이 올바르지 않습니다. |

### MemberService 수정

기존 `IllegalArgumentException` → `CustomException(ErrorCode.xxx)` 으로 교체

---

## 11. 상품 API 구현

### 추가한 파일 목록

```
domain/item/
    dto/
        CategoryRequest.java      → 카테고리 생성 요청 DTO
        CategoryResponse.java     → 카테고리 응답 DTO
        ItemRequest.java          → 상품 등록/수정 요청 DTO
        ItemResponse.java         → 상품 응답 DTO (카테고리명, 옵션 목록 포함)
        ItemOptionRequest.java    → 옵션 생성 요청 DTO
        ItemOptionResponse.java   → 옵션 응답 DTO
    service/
        CategoryService.java      → 카테고리 CRUD
        ItemService.java          → 상품 CRUD + 검색 + 페이지네이션
    controller/
        CategoryController.java   → /api/categories
        ItemController.java       → /api/items
```

### API 목록

| 메서드 | URL | 권한 | 설명 |
|---|---|---|---|
| GET | /api/categories | 누구나 | 카테고리 전체 조회 |
| POST | /api/categories | ADMIN | 카테고리 생성 |
| DELETE | /api/categories/{id} | ADMIN | 카테고리 삭제 |
| GET | /api/items | 누구나 | 상품 목록 (페이지네이션, 카테고리 필터) |
| GET | /api/items/search | 누구나 | 상품 키워드 검색 |
| GET | /api/items/{id} | 누구나 | 상품 상세 조회 |
| POST | /api/items | ADMIN | 상품 등록 |
| PUT | /api/items/{id} | ADMIN | 상품 수정 |
| DELETE | /api/items/{id} | ADMIN | 상품 삭제 |

### SecurityConfig 변경 사항

- `@EnableMethodSecurity` 추가 → `@PreAuthorize` 어노테이션 활성화
- GET /api/items/**, GET /api/categories/** → 비로그인도 허용

### 페이지네이션 응답 형식

```json
{
  "content": [...],         // 실제 데이터 목록
  "totalElements": 100,     // 전체 데이터 수
  "totalPages": 5,          // 전체 페이지 수
  "number": 0,              // 현재 페이지 번호
  "size": 20                // 페이지 크기
}
```

### 테스트 순서 (앱 실행 후)

1. DB에서 계정의 `role`을 `ADMIN`으로 변경
```bash
/usr/local/mysql/bin/mysql -u root -pcjstk12 -e \
  "USE shopping_mall; UPDATE member SET role='ADMIN' WHERE email='alice@email.com';"
```
2. Swagger에서 ADMIN 계정으로 로그인 → Authorize 등록
3. `POST /api/categories` — 카테고리 생성
4. `POST /api/items` — 상품 등록 (categoryId 필요)
5. `GET /api/items` — 상품 목록 조회 (토큰 없이도 가능) ✅

### 트러블슈팅: ADMIN 계정인데 POST /api/items 403

**증상**
DB에서 role=ADMIN으로 바꿨는데 상품 등록 시 403

**원인**
`JwtAuthenticationFilter`에서 토큰의 role을 읽지 않고 항상 `ROLE_USER`로 고정했음
```java
// 문제 코드
List.of(new SimpleGrantedAuthority("ROLE_USER"))  // 무조건 USER
```

**해결**
1. `JwtUtil.generateToken()`에 role 파라미터 추가 → 토큰에 role 포함
2. `JwtUtil.getRole()` 메서드 추가
3. `JwtAuthenticationFilter`에서 토큰의 role로 권한 설정
```java
// 수정 후
String role = jwtUtil.getRole(token);
List.of(new SimpleGrantedAuthority("ROLE_" + role))  // 토큰의 실제 role 사용
```
> ⚠️ 수정 후 반드시 **재로그인** 필요 (기존 토큰에는 role이 없음)

### 트러블슈팅: 상품 등록 후 options: []

**증상**
`POST /api/items` 응답에서 options 배열이 빈 배열로 반환됨

**원인**
JPA 1차 캐시 문제. 옵션을 DB에 저장했지만 같은 트랜잭션 내에서 item을 다시 조회하면 Hibernate가 캐시된 객체(옵션 없는)를 반환함

**해결**
저장한 옵션을 리스트로 직접 수집해서 DTO에 전달
```java
List<ItemOption> savedOptions = new ArrayList<>();
// ... 옵션 저장 시 savedOptions.add(itemOptionRepository.save(option));
return new ItemResponse(savedItem, savedOptions);  // 직접 전달
```

### 트러블슈팅: GET /api/items 403 (토큰 없이)

**증상**
비로그인 상태에서 `GET /api/items` 요청 시 403

**원인**
SecurityConfig에서 `"/api/items/**"` 패턴은 `/api/items/1` 같은 경로만 매칭되고
`/api/items` (슬래시 없이 끝나는 경로)는 매칭되지 않음

**해결**
```java
// 수정 전
.requestMatchers(GET, "/api/items/**", "/api/categories/**").permitAll()

// 수정 후: 경로 자체도 명시적으로 추가
.requestMatchers(GET, "/api/items", "/api/items/**", "/api/categories", "/api/categories/**").permitAll()
```

### 트러블슈팅: Swagger에서 pageable 필수 입력 오류

**증상**
`GET /api/items` Swagger 실행 시 "For 'pageable': Required field is not provided." 오류

**원인**
Swagger가 `Pageable`을 복잡한 객체로 인식해서 필수 필드로 표시함

**해결**
컨트롤러 파라미터에 `@ParameterObject` 어노테이션 추가
```java
// 수정 전
public ResponseEntity<Page<ItemResponse>> getItems(
        @PageableDefault(...) Pageable pageable)

// 수정 후
public ResponseEntity<Page<ItemResponse>> getItems(
        @ParameterObject @PageableDefault(...) Pageable pageable)
```
→ Swagger에서 page / size / sort 개별 필드로 표시됨

---

## 다음 단계
- [x] 앱 첫 실행 성공 확인
- [x] Security 설정 및 Swagger 확인
- [x] 패키지 구조 설계
- [x] Entity 클래스 작성 (7개)
- [x] DB 테이블 자동 생성 확인
- [x] Repository 인터페이스 작성 (8개)
- [x] JWT 라이브러리 추가
- [x] JwtUtil, JwtAuthenticationFilter 작성
- [x] 회원가입 / 로그인 API 구현
- [x] Swagger로 JWT 인증 흐름 전체 테스트 성공
- [x] GlobalExceptionHandler 구현
- [x] 상품 API 구현 (카테고리, 상품, 옵션) + Swagger 테스트 성공
- [x] 장바구니 API 구현
- [x] 주문 API 구현

---

## 12. 장바구니 API 구현

### 추가한 파일 목록

```
domain/cart/
    dto/
        CartAddRequest.java     → 상품 담기 요청 DTO (itemOptionId, quantity)
        CartUpdateRequest.java  → 수량 변경 요청 DTO (quantity)
        CartItemResponse.java   → 장바구니 항목 응답 DTO
    service/
        CartService.java        → 장바구니 비즈니스 로직
    controller/
        CartController.java     → /api/cart
```

### API 목록

| 메서드 | URL | 설명 |
|---|---|---|
| GET | /api/cart | 내 장바구니 조회 |
| POST | /api/cart/items | 상품 담기 (중복이면 수량 증가) |
| PATCH | /api/cart/items/{itemOptionId} | 수량 변경 |
| DELETE | /api/cart/items/{itemOptionId} | 상품 삭제 |

### 핵심 로직: 상품 담기 중복 처리

```java
Optional<CartItem> existing = cartItemRepository
        .findByCartIdAndItemOptionId(cart.getId(), itemOptionId);

if (existing.isPresent()) {
    existing.get().addQuantity(quantity);  // 이미 있으면 수량 증가
} else {
    cartItemRepository.save(new CartItem(cart, itemOption, quantity));  // 없으면 새로 추가
}
```

### MemberService 수정

회원가입 시 장바구니 자동 생성 추가
```java
Member savedMember = memberRepository.save(member);
cartRepository.save(new Cart(savedMember));  // 장바구니 자동 생성
```

### Swagger 테스트 결과
- `POST /api/cart/items` → 201 Created ✅
- `GET /api/cart` → 200 OK (담긴 상품 목록) ✅
- `PATCH /api/cart/items/{itemOptionId}` → 200 OK ✅
- `DELETE /api/cart/items/{itemOptionId}` → 204 No Content ✅

---

## 13. 주문 API 구현

### 추가한 파일 목록

```
domain/order/
    dto/
        CreateOrderRequest.java   → 주문 생성 요청 DTO (deliveryAddress)
        OrderItemResponse.java    → 주문 상품 응답 DTO
        OrderResponse.java        → 주문 응답 DTO (주문 상품 목록 포함)
    service/
        OrderService.java         → 주문 비즈니스 로직
    controller/
        OrderController.java      → /api/orders
```

### API 목록

| 메서드 | URL | 설명 |
|---|---|---|
| POST | /api/orders | 주문 생성 (장바구니 → 주문) |
| GET | /api/orders | 내 주문 목록 (페이지네이션) |
| GET | /api/orders/{orderId} | 주문 상세 조회 |
| DELETE | /api/orders/{orderId} | 주문 취소 (PENDING 상태만) |

### 핵심 로직: 장바구니 → 주문 변환

```java
// 1. 장바구니 상품으로 OrderItem 생성 (가격 스냅샷)
for (CartItem cartItem : cart.getCartItems()) {
    OrderItem orderItem = OrderItem.builder()
            .order(order)
            .itemOption(cartItem.getItemOption())
            .quantity(cartItem.getQuantity())
            .orderPrice(cartItem.getItemOption().getItem().getPrice())  // 현재가격 스냅샷
            .build();
    order.getOrderItems().add(orderItem);
}

// 2. 주문 저장 후 장바구니 비우기
orderRepository.save(order);
cart.getCartItems().clear();  // orphanRemoval = true 이므로 DB에서도 자동 삭제
```

### 주문 취소 로직

```java
// Order.java 엔티티에 취소 메서드 있음
public void cancel() {
    if (this.status != Status.PENDING) {
        throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }
    this.status = Status.CANCELLED;
}
```
→ PENDING 상태가 아니면 취소 불가. 상태 변경만으로 취소 처리 (물리적 삭제 X)

### 트러블슈팅: Public Key Retrieval is not allowed

**증상**
```
SQLNonTransientConnectionException: Public Key Retrieval is not allowed
```

**원인**
MySQL 8.x는 기본 인증 방식이 `caching_sha2_password`로 변경됨.
클라이언트가 처음 연결할 때 서버 공개키를 받아와야 하는데, 기본값이 차단되어 있음.

**해결**
`application.properties`의 DB URL에 옵션 추가
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shopping_mall?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
```

### 트러블슈팅: git amend + force push 후 잔디 안 심어짐

**증상**
커밋 author를 수정하기 위해 `--amend --reset-author` 후 `--force push` 했더니 GitHub 잔디가 반영되지 않음

**원인**
GitHub이 force push로 덮어쓴 커밋의 contribution 처리를 늦게 하거나 누락하는 경우가 있음

**해결**
빈 커밋 하나를 새로 push하니 즉시 반영됨
```bash
git commit --allow-empty -m "test"
git push origin main
```
→ 앞으로 author 수정이 필요하면 amend 대신 **새 커밋**으로 처리
