# Spring Boot 쇼핑몰 개발 개념 정리

> 프로젝트 진행하면서 배운 개념들을 정리한 파일

---

## 목차
1. [프로젝트 구조](#1-프로젝트-구조)
2. [어노테이션 정리](#2-어노테이션-정리)
3. [JPA / Entity 개념](#3-jpa--entity-개념)
4. [연관관계](#4-연관관계)
5. [Repository](#5-repository)
6. [Spring Security](#6-spring-security)
7. [JWT](#7-jwt)
8. [DTO](#8-dto)
9. [레이어별 역할 정리](#9-레이어별-역할-정리)
10. [Swagger](#10-swagger)
11. [Swagger 인증 설정](#11-swagger-인증-설정)
12. [Gradle / 빌드 도구](#12-gradle--빌드-도구)
13. [전역 예외 처리 (GlobalExceptionHandler)](#13-전역-예외-처리-globalexceptionhandler)
14. [컨트롤러 파라미터 어노테이션](#14-컨트롤러-파라미터-어노테이션)
15. [페이지네이션 (Pagination)](#15-페이지네이션-pagination)
16. [메서드 레벨 권한 제어](#16-메서드-레벨-권한-제어)
17. [JWT에 권한(role) 포함하기](#17-jwt에-권한role-포함하기)
18. [Spring Security URL 패턴 주의사항](#18-spring-security-url-패턴-주의사항)
19. [Swagger와 Pageable — @ParameterObject](#19-swagger와-pageable--parameterobject)
20. [Optional과 stream](#20-optional과-stream)
19. [Swagger와 Pageable — @ParameterObject](#19-swagger와-pageable--parameterobject)

---

## 1. 프로젝트 구조

```
shop/
└── src/main/java/shop/example/shop/
    ├── config/         → 설정 파일 (Security, Swagger 등)
    ├── domain/         → 핵심 비즈니스 코드
    │   ├── member/
    │   │   ├── entity/       → DB 테이블과 연결되는 클래스
    │   │   ├── repository/   → DB 조회/저장 담당
    │   │   ├── service/      → 비즈니스 로직
    │   │   ├── controller/   → API 요청/응답 처리
    │   │   └── dto/          → 요청/응답 데이터 형식 정의
    │   ├── item/
    │   ├── cart/
    │   └── order/
    └── global/
        ├── exception/  → 공통 에러 처리
        └── response/   → 공통 응답 형식
```

### 각 레이어 역할 흐름
```
HTTP 요청
    ↓
Controller   → 요청을 받고 응답을 반환 (API 엔드포인트)
    ↓
Service      → 실제 비즈니스 로직 처리
    ↓
Repository   → DB에 저장/조회
    ↓
DB (MySQL)
```

---

## 2. 어노테이션 정리

### 클래스 레벨

| 어노테이션 | 위치 | 역할 |
|---|---|---|
| `@Entity` | 클래스 | 이 클래스가 DB 테이블과 연결됨 |
| `@Table(name = "xxx")` | 클래스 | 연결할 테이블 이름 지정 |
| `@Getter` | 클래스 | Lombok: 모든 필드 getter 자동 생성 |
| `@Builder` | 클래스/생성자 | Lombok: 빌더 패턴 자동 생성 |
| `@NoArgsConstructor` | 클래스 | Lombok: 기본 생성자 자동 생성 |
| `@Configuration` | 클래스 | Spring 설정 클래스임을 표시 |
| `@EnableWebSecurity` | 클래스 | Spring Security 활성화 |

### 필드 레벨

| 어노테이션 | 역할 |
|---|---|
| `@Id` | 기본키(PK) 지정 |
| `@GeneratedValue(strategy = IDENTITY)` | AUTO_INCREMENT |
| `@Column(nullable = false)` | NOT NULL 제약조건 |
| `@Column(unique = true)` | UNIQUE 제약조건 |
| `@Column(length = 100)` | VARCHAR(100) 길이 지정 |
| `@Enumerated(EnumType.STRING)` | Enum을 문자열로 DB에 저장 |
| `@CreationTimestamp` | INSERT 시 자동으로 현재 시간 저장 |
| `@UpdateTimestamp` | UPDATE 시 자동으로 현재 시간 갱신 |

---

## 3. JPA / Entity 개념

### JPA란?
Java 코드로 DB를 다루는 기술. SQL을 직접 안 써도 됨.
- `Hibernate`가 JPA의 실제 구현체 (Spring Boot에 기본 포함)

### ddl-auto 옵션
`application.properties`의 `spring.jpa.hibernate.ddl-auto` 설정값

| 값 | 동작 |
|---|---|
| `create` | 앱 시작할 때 테이블 삭제 후 재생성 (데이터 날아감) |
| `update` | 변경사항만 반영. 없으면 생성 (개발 단계에서 사용) |
| `validate` | Entity와 테이블 구조가 맞는지만 확인 |
| `none` | 아무것도 안 함 (운영 환경에서 사용) |

### Builder 패턴
객체 생성 시 어떤 값이 어떤 필드인지 명확하게 표현

```java
// Builder 없이
new Member("alice@email.com", "1234", "홍길동", null, null, Role.USER);
// 뭐가 뭔지 모름 ❌

// Builder 사용
Member.builder()
    .email("alice@email.com")
    .password("1234")
    .name("홍길동")
    .build();
// 명확함 ✅
```

### @NoArgsConstructor(access = PROTECTED)
JPA는 내부적으로 기본 생성자가 필요함.
`PROTECTED`로 설정하면 외부에서 `new Member()` 직접 생성을 막고 Builder만 쓰도록 강제함.

---

## 4. 연관관계

### 종류

| 어노테이션 | 관계 | 예시 |
|---|---|---|
| `@ManyToOne` | N:1 | Item → Category (상품 N개 : 카테고리 1개) |
| `@OneToMany` | 1:N | Category → Item (카테고리 1개 : 상품 N개) |
| `@OneToOne` | 1:1 | Member ↔ Cart (회원 1명 : 장바구니 1개) |

### FK는 누가 갖나?
항상 **N 쪽(Many 쪽)** 이 FK를 가짐

```java
// Item (N) → Category (1)
@ManyToOne
@JoinColumn(name = "category_id")  // item 테이블에 category_id 컬럼이 생김
private Category category;
```

### mappedBy
양방향 관계에서 FK를 갖지 않는 쪽에 `mappedBy`를 붙임
"FK는 내가 아니라 상대방이 갖고 있어"라는 의미

```java
// Category (1) 쪽
@OneToMany(mappedBy = "category")  // Item 클래스의 category 필드가 FK 주인
private List<Item> items;
```

### FetchType.LAZY (지연 로딩)
연관된 데이터를 **즉시 가져오지 않고**, 실제로 사용할 때 그때 조회함
→ 성능 최적화. 기본값으로 항상 LAZY 사용 권장

```java
@ManyToOne(fetch = FetchType.LAZY)
private Category category;
// item 조회할 때 category는 아직 안 가져옴
// item.getCategory() 호출할 때 그때 DB 조회
```

### cascade & orphanRemoval
```java
@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
private List<CartItem> cartItems;
```
- `cascade = ALL` → Cart 저장/삭제 시 CartItem도 함께 처리
- `orphanRemoval = true` → 목록에서 제거된 CartItem은 DB에서도 자동 삭제

---

## 5. Repository

### JpaRepository
`JpaRepository<Entity 타입, PK 타입>`을 상속받으면 기본 CRUD 자동 생성

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
}
// 아무것도 안 써도 아래 메서드가 자동으로 생김:
// save(), findById(), findAll(), deleteById(), count() 등
```

### 메서드 이름으로 쿼리 생성
메서드 이름 규칙에 따라 JPA가 SQL을 자동 생성해줌

```java
// 메서드 이름 → 자동 생성되는 SQL
findByEmail(String email)
→ SELECT * FROM member WHERE email = ?

findByCategoryIdAndStatus(Long categoryId, Status status)
→ SELECT * FROM item WHERE category_id = ? AND status = ?

existsByEmail(String email)
→ SELECT COUNT(*) > 0 FROM member WHERE email = ?
```

### Optional
"있을 수도 없을 수도 있는 값"을 표현하는 타입. null 대신 사용.

```java
Optional<Member> member = memberRepository.findByEmail("alice@email.com");

// 사용할 때
member.isPresent()           // 값이 있으면 true
member.get()                 // 값 꺼내기 (없으면 예외 발생)
member.orElseThrow(...)      // 없으면 예외 던지기 (가장 많이 씀)
```

### Page / Pageable
목록을 페이지 단위로 나눠서 조회

```java
// Repository
Page<Item> findByStatus(Item.Status status, Pageable pageable);

// 사용할 때 (Controller에서)
Pageable pageable = PageRequest.of(0, 20);  // 0번 페이지, 한 페이지에 20개
Page<Item> items = itemRepository.findByStatus(Status.SELLING, pageable);
```

---

## 6. Spring Security

### 역할
모든 HTTP 요청을 가로채서 인증/인가를 처리함

### SecurityFilterChain
어떤 경로를 허용하고 막을지 설정하는 핵심 클래스

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())           // REST API는 CSRF 불필요
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()  // 로그인/회원가입은 누구나
            .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 관리자만
            .anyRequest().authenticated()        // 나머지는 로그인 필요
        );
    return http.build();
}
```

### CSRF란?
Cross-Site Request Forgery (사이트 간 요청 위조) 공격 방어 기능.
REST API + JWT 방식에선 필요 없어서 꺼둠.

---

## 7. JWT

### 구조
```
헤더.페이로드.서명
eyJhbGci....eyJzdWIi....abc123
```
- **헤더**: 알고리즘 정보
- **페이로드**: 실제 데이터 (회원 ID, 이메일 등)
- **서명**: 위조 방지 (서버만 아는 비밀키로 생성)

### 인증 흐름
```
1. POST /api/auth/login → 서버가 JWT 발급
2. 이후 요청마다 헤더에 포함
   Authorization: Bearer eyJhbGci...
3. 서버가 토큰 검증 후 회원 확인
```

### 세션 vs JWT
| | 세션 | JWT |
|---|---|---|
| 저장 위치 | 서버 메모리 | 토큰 자체 |
| 서버 부담 | 있음 | 없음 |
| REST API 궁합 | 별로 | 좋음 ✅ |

---

## 8. DTO

### DTO(Data Transfer Object)란?
요청/응답 데이터 형식을 정의하는 클래스. Entity를 직접 반환하지 않고 DTO로 변환해서 반환함.

**Entity를 직접 반환하면 안 되는 이유:**
- 비밀번호 같은 민감한 정보가 노출될 수 있음
- DB 구조가 그대로 외부에 노출됨
- API 응답 형식과 DB 구조가 강하게 결합됨

### 데이터 흐름
```
[요청]
클라이언트 → RequestDTO → Service → Entity → DB

[응답]
DB → Entity → Service → ResponseDTO → 클라이언트
```

### 유효성 검증 어노테이션
Request DTO에 붙여서 잘못된 입력을 걸러냄. Controller에서 `@Valid`를 붙여야 동작함.

| 어노테이션 | 역할 |
|---|---|
| `@NotBlank` | null, 빈 문자열, 공백 불가 |
| `@Email` | 이메일 형식 검증 |
| `@Size(min = 8)` | 최소 길이 검증 |
| `@NotNull` | null 불가 |

```java
// DTO
@NotBlank(message = "이메일은 필수입니다.")
@Email(message = "이메일 형식이 올바르지 않습니다.")
private String email;

// Controller
public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
    // @Valid가 없으면 검증이 실행되지 않음!
}
```

---

## 9. 레이어별 역할 정리

### Controller
- HTTP 요청을 받고 응답을 반환
- 비즈니스 로직을 직접 처리하지 않음 → Service에 위임
- 최대한 얇게 유지

```java
@PostMapping("/signup")
public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
    return ResponseEntity.status(201).body(memberService.signup(request));
}
```

### Service
- 실제 비즈니스 로직이 여기에 있음
- `@Transactional` — DB 작업을 하나의 단위로 묶음 (중간에 에러 나면 전부 취소)
- `readOnly = true` — 조회만 하는 메서드에 붙이면 성능 최적화

```java
@Transactional(readOnly = true)  // 클래스 전체 기본값
public class MemberService {

    @Transactional  // 쓰기 작업만 별도 지정
    public MemberResponse signup(SignupRequest request) { ... }
}
```

### @Transactional이란?
DB 작업을 하나의 묶음으로 처리. 중간에 에러가 나면 전부 롤백(취소)됨.

```
회원가입 시:
1. 이메일 중복 확인  ─┐
2. 회원 생성        ─┤ @Transactional로 묶임
3. DB 저장          ─┘
→ 3번에서 에러 나면 1,2도 취소됨
```

### @AuthenticationPrincipal
JwtFilter에서 SecurityContext에 저장한 인증 정보를 Controller에서 꺼낼 때 사용.

```java
// JwtFilter에서 저장
new UsernamePasswordAuthenticationToken(memberId, null, authorities)
//                                       ↑ principal

// Controller에서 꺼내기
public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal Long memberId) {
    // memberId = 로그인한 회원의 ID
}
```

### BCrypt 비밀번호 암호화
단방향 해시 → 복호화 불가능. 같은 비밀번호도 매번 다른 해시값 생성.

```java
// 회원가입 시 암호화
String encoded = passwordEncoder.encode("1234abcd!");
// → "$2a$10$abc123xyz..."  (매번 다른 값)

// 로그인 시 비교
passwordEncoder.matches("1234abcd!", encoded);  // true
```

---

## 10. Swagger

### 역할
코드에서 자동으로 API 문서를 만들어주고 브라우저에서 바로 테스트할 수 있는 도구.
별도 사이트가 아니라 **앱 안에 내장된 페이지** (앱 실행 중일 때만 접근 가능)

### 접속 URL
```
http://localhost:8080/swagger-ui.html
```

### 설정
```properties
# application.properties
springdoc.swagger-ui.path=/swagger-ui.html
```

---

## 11. Swagger 인증 설정

### Authorize 버튼이 안 보이는 이유
Swagger에 JWT 인증 방식을 알려주는 설정이 없으면 자물쇠 버튼이 생기지 않음.
`SwaggerConfig.java`에 SecurityScheme을 등록해야 함.

### Swagger에서 JWT 토큰으로 테스트하는 방법
1. 로그인 API 호출 → `accessToken` 복사
2. Swagger 우측 상단 **Authorize 🔒** 클릭
3. 토큰 값만 입력 (`Bearer` 없이 토큰만)
4. Authorize 클릭 → 이후 모든 요청에 자동으로 헤더 포함

> ⚠️ 앱 재시작하면 Authorize 초기화됨 → 다시 등록 필요

### 토큰이 헤더에 포함된 요청 형태
```bash
curl -H 'Authorization: Bearer eyJhbGci...'
```

---

## 12. Gradle / 빌드 도구 (구 11번)

### 역할
프로젝트 빌드, 의존성(라이브러리) 관리 도구

### 주요 명령어
```bash
./gradlew bootRun       # 앱 실행
./gradlew build         # 빌드 (jar 파일 생성)
./gradlew test          # 테스트 실행
./gradlew clean         # 빌드 결과물 삭제
```

### build.gradle 의존성 추가 방법
```groovy
dependencies {
    implementation '그룹:아티팩트:버전'   // 실제 실행에 필요한 라이브러리
    runtimeOnly '...'                    // 실행할 때만 필요 (MySQL 드라이버 등)
    compileOnly '...'                    // 컴파일할 때만 필요 (Lombok 등)
    testImplementation '...'             // 테스트 코드에서만 사용
}
```

### Gradle Wrapper (gradlew)
프로젝트에 포함된 Gradle 실행 스크립트.
Gradle이 로컬에 없어도 자동으로 다운로드해서 실행해줌.
팀 전체가 동일한 버전을 쓸 수 있게 해줌.

---

## 13. 전역 예외 처리 (GlobalExceptionHandler)

### 왜 필요한가?

예외를 서비스에서 던져도 잡아주는 핸들러가 없으면 Spring Security가 대신 처리해서 엉뚱한 HTTP 상태코드(주로 403)가 나옴.
`@RestControllerAdvice`를 사용하면 모든 컨트롤러에서 발생한 예외를 **한 곳에서 잡아서** 통일된 JSON 에러 응답을 반환할 수 있음.

### 구조

```
global/exception/
    ErrorCode.java              → 에러 코드 열거형
    CustomException.java        → 커스텀 예외 클래스
    ErrorResponse.java          → 에러 응답 DTO
    GlobalExceptionHandler.java → 예외 처리기
```

### 사용 방법

**1. 에러 코드 정의 (ErrorCode.java)**
```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String message;
}
```

**2. 예외 던지기 (Service에서)**
```java
// 기존: IllegalArgumentException 사용 → 핸들러가 잡지 못해 403 됨
throw new IllegalArgumentException("회원 없음");

// 변경: CustomException 사용 → GlobalExceptionHandler가 잡아서 정확한 응답 반환
throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
```

**3. 처리기 (GlobalExceptionHandler.java)**
```java
@RestControllerAdvice  // 모든 @RestController의 예외를 이 클래스에서 처리
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new ErrorResponse(e.getErrorCode()));
    }
}
```

**4. 에러 응답 형식**
```json
{
  "code": "MEMBER_NOT_FOUND",
  "message": "존재하지 않는 회원입니다."
}
```

### @ExceptionHandler vs @RestControllerAdvice

| | @ExceptionHandler | @RestControllerAdvice |
|---|---|---|
| 범위 | 특정 컨트롤러 내부만 | **모든 컨트롤러** |
| 위치 | 컨트롤러 내부 | 별도 클래스 |
| 사용 | 거의 안 씀 | 실무에서 표준 |

---

## 14. 컨트롤러 파라미터 어노테이션

### @PathVariable — URL 경로에서 값 추출

```java
// URL: GET /api/items/5
@GetMapping("/{id}")
public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
    // id = 5
}
```

### @RequestParam — URL 쿼리 파라미터에서 값 추출

```java
// URL: GET /api/items?categoryId=1&page=0&size=20
@GetMapping
public ResponseEntity<?> getItems(
        @RequestParam(required = false) Long categoryId,  // 없어도 됨 (null)
        @RequestParam(defaultValue = "0") int page        // 없으면 기본값 0
) { }
```

### @RequestBody — HTTP 요청 바디(JSON)를 객체로 변환

```java
// POST 요청의 JSON 바디를 ItemRequest 객체로 자동 변환
@PostMapping
public ResponseEntity<?> createItem(@Valid @RequestBody ItemRequest request) { }
```

### 차이 요약

| 어노테이션 | 위치 | 예시 |
|---|---|---|
| `@PathVariable` | URL 경로 | `/api/items/{id}` |
| `@RequestParam` | URL 쿼리 | `/api/items?categoryId=1` |
| `@RequestBody` | HTTP 바디 | POST/PUT의 JSON 본문 |

---

## 15. 페이지네이션 (Pagination)

데이터가 많을 때 전부 가져오지 않고 **페이지 단위로 나눠서** 가져오는 방식.
예: 상품 100개를 한 번에 반환하지 않고 20개씩 5페이지로 나눔.

### Pageable — 페이지 요청 정보

```java
// 클라이언트 요청: GET /api/items?page=0&size=20&sort=createdAt,desc
// Spring이 자동으로 Pageable 객체로 변환해줌

@GetMapping
public ResponseEntity<Page<ItemResponse>> getItems(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
) { }
```

- `page=0` → 첫 번째 페이지 (0부터 시작)
- `size=20` → 한 페이지에 20개
- `sort=createdAt,desc` → 최신순 정렬

### Page\<T\> — 페이지 응답 형식

```java
// Repository
Page<Item> findByStatus(Item.Status status, Pageable pageable);

// Service: Page<Item> → Page<ItemResponse> 변환
return items.map(ItemResponse::new);  // Page가 map() 지원
```

### 응답 구조

```json
{
  "content": [{ "id": 1, "name": "청바지" }, ...],  // 실제 데이터
  "totalElements": 100,  // 전체 데이터 수
  "totalPages": 5,       // 전체 페이지 수
  "number": 0,           // 현재 페이지 (0부터)
  "size": 20,            // 페이지 크기
  "first": true,         // 첫 페이지 여부
  "last": false          // 마지막 페이지 여부
}
```

---

## 16. 메서드 레벨 권한 제어


### @PreAuthorize

메서드 실행 **전에** 권한을 확인하는 어노테이션.
SecurityConfig의 URL 기반 권한 설정과 달리 **메서드 단위로 세밀하게** 제어 가능.

```java
// 관리자만 접근 가능
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ResponseEntity<ItemResponse> createItem(...) { }

// 로그인한 사용자만 접근 가능
@PreAuthorize("isAuthenticated()")
@GetMapping("/me")
public ResponseEntity<MemberResponse> getMyInfo(...) { }
```

### 활성화 방법

SecurityConfig에 `@EnableMethodSecurity` 추가해야 동작함.

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 이게 없으면 @PreAuthorize가 무시됨!
public class SecurityConfig { }
```

### URL 기반 vs 메서드 기반 권한 비교

| | SecurityConfig (URL 기반) | @PreAuthorize (메서드 기반) |
|---|---|---|
| 설정 위치 | SecurityConfig.java | 각 Controller 메서드 |
| 제어 단위 | URL 패턴 | 메서드 |
| 유연성 | 낮음 | 높음 |
| 실무 | 큰 단위 구분용 | 세밀한 권한 제어용 |

> 둘 다 함께 사용하는 것이 일반적. URL 기반으로 큰 틀을 잡고, 메서드 기반으로 세밀하게 조정.

---

## 17. JWT에 권한(role) 포함하기

### 왜 role을 토큰에 넣어야 하나?

필터는 매 요청마다 실행되는데, DB를 조회해서 권한을 확인하면 성능이 낮아짐.
토큰 자체에 role을 넣으면 DB 조회 없이 바로 권한을 알 수 있음.

### 흐름

```
[로그인]
MemberService → jwtUtil.generateToken(id, email, role)
                                               ↑ role을 토큰에 포함

[이후 요청]
JwtAuthenticationFilter → jwtUtil.getRole(token) → "ADMIN" or "USER"
                        → new SimpleGrantedAuthority("ROLE_" + role)
                        → SecurityContext에 저장
                        → @PreAuthorize("hasRole('ADMIN')") 가 이걸 확인
```

### 주의: 토큰 재발급 필요

role을 토큰에 추가하는 로직을 변경한 후 **기존 토큰은 role 정보가 없음**.
반드시 재로그인해서 새 토큰을 발급받아야 함.

---

## 18. Spring Security URL 패턴 주의사항

### `/**` vs 경로 자체

```java
// "/api/items/**" → /api/items/1, /api/items/search 는 허용
//                 → /api/items (슬래시 없이 끝나는 것)는 허용 안 됨!

// 올바른 설정: 경로 자체와 하위 경로를 모두 명시
.requestMatchers(GET, "/api/items", "/api/items/**").permitAll()
```

### 규칙 순서가 중요함

Spring Security는 위에서부터 순서대로 규칙을 적용함. 먼저 매칭되는 규칙이 적용됨.

```java
.requestMatchers("/api/auth/**").permitAll()       // 1순위
.requestMatchers(GET, "/api/items/**").permitAll() // 2순위
.anyRequest().authenticated()                       // 나머지 전부
```

---

## 19. Swagger와 Pageable — @ParameterObject

### 문제

`Pageable`을 컨트롤러 파라미터로 쓰면 Swagger가 복잡한 객체로 인식해서
"Required field is not provided" 에러를 내거나 이상하게 표시됨.

### 해결

`@ParameterObject` 어노테이션 추가 → `page`, `size`, `sort` 개별 필드로 표시됨

```java
// import org.springdoc.core.annotations.ParameterObject;

@GetMapping
public ResponseEntity<Page<ItemResponse>> getItems(
        @ParameterObject  // ← 이거 추가
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
) { }
```

Swagger에서 보이는 모습:
- `page` — 페이지 번호 (기본값 0)
- `size` — 페이지 크기 (기본값 20)
- `sort` — 정렬 기준 (예: `createdAt,desc`)

---

## 20. Optional과 stream

### Optional — 값이 있을 수도 없을 수도 있는 박스

DB 조회 결과가 없을 수 있을 때 `Optional`로 감싸서 반환함.
null 체크 대신 메서드로 처리할 수 있어서 안전함.

```java
// Repository 메서드
Optional<CartItem> findByCartIdAndItemOptionId(Long cartId, Long itemOptionId);

// 사용 방법
Optional<CartItem> existing = cartItemRepository.findByCartIdAndItemOptionId(...);

existing.isPresent()   // 값이 있으면 true
existing.get()         // 값 꺼내기 (없으면 예외 발생)
existing.orElseThrow() // 없으면 예외 던지기
```

### 장바구니 중복 담기 패턴

```java
Optional<CartItem> existing = cartItemRepository
        .findByCartIdAndItemOptionId(cart.getId(), itemOptionId);

if (existing.isPresent()) {
    existing.get().addQuantity(quantity);  // 있으면 수량 증가
} else {
    cartItemRepository.save(new CartItem(cart, itemOption, quantity));  // 없으면 새로 추가
}
```

---

### stream — 리스트를 변환하는 방법

엔티티 리스트 → DTO 리스트로 변환할 때 주로 사용.

```java
// 기존 for문 방식
List<CartItemResponse> result = new ArrayList<>();
for (CartItem item : cartItems) {
    result.add(new CartItemResponse(item));
}

// stream 방식 (결과는 동일)
List<CartItemResponse> result = cartItems.stream()
        .map(CartItemResponse::new)  // 각 CartItem → CartItemResponse 변환
        .toList();
```

- `.stream()` — 리스트를 순회 준비
- `.map(변환함수)` — 각 요소를 다른 타입으로 변환
- `.toList()` — 다시 List로 모으기
- `CartItemResponse::new` — `item -> new CartItemResponse(item)` 의 축약
