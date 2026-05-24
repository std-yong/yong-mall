# 🛍️ Shopping Mall API

Spring Boot 기반 의류 쇼핑몰 REST API 서버

> 인프라 학습 경험을 코드 영역으로 확장하기 위해 진행한 백엔드 개인 프로젝트.
> 도메인 분리 → JWT 인증/인가 → 트랜잭션·N+1·재고 정합성 → 리팩토링 → 테스트 환경 구축까지 한 사이클을 직접 다뤘다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 (Temurin) |
| Framework | Spring Boot 3.5.13, Spring Web, Spring Data JPA, Spring Security |
| ORM / DB | Hibernate (JPA), MySQL 8.0 (운영), H2 (테스트) |
| 인증/인가 | JWT (jjwt 0.12.6), BCrypt |
| API 문서 | SpringDoc OpenAPI (Swagger UI) |
| 빌드 / 테스트 | Gradle, JUnit 5 |

### 기술 선택 이유 (회고 관점)

- **JPA (vs MyBatis)**: 도메인 중심 설계 학습이 목표라 객체 그래프를 그대로 다루는 JPA가 적합. N+1·1차 캐시의 추상화 비용을 직접 부딪치며 학습한 점이 가장 큰 수확.
- **JWT (vs Session)**: 향후 팀 프로젝트에서 프론트/백엔드 분리 구조를 가정. Redis 세션 스토어 운영 경험이 없는 상황에서 세션 클러스터링 부담을 피하기 위해 Stateless 인증 선택.
- **MySQL 8.0 (vs PostgreSQL)**: 국내 채용 시장에서 가장 빈번한 RDB. 도메인 복잡도가 PostgreSQL의 고급 기능을 요구할 수준은 아니었다.
- **H2 (테스트 전용)**: 외부 인프라 의존 없이 테스트 컨텍스트가 뜨도록 분리. 향후 Testcontainers로 옮기면 MySQL 8 고유 동작(`caching_sha2_password`)까지 검증 가능하다는 점은 한계로 인지.

---

## 주요 기능

- **회원 인증/인가** — 회원가입(BCrypt) → 로그인(JWT 발급) → 토큰 기반 인가. 가입 시 장바구니 자동 생성
- **상품/카테고리** — 카테고리 CRUD, 상품 CRUD, 옵션(사이즈/색상/재고), 키워드 검색, 페이지네이션 (ADMIN 권한)
- **장바구니** — 회원 1인 1장바구니. 동일 옵션 재담기 시 수량 자동 합산
- **주문** — 장바구니 → 주문 변환 시 **주문 시점 가격 스냅샷** 저장. PENDING 상태에서만 취소 가능
- **재고 차감/복구** — 주문 생성 시 차감, 취소 시 복구. 트랜잭션 단위 정합성
- **전역 예외 처리** — `ErrorCode` + `CustomException` + `@RestControllerAdvice`로 응답 포맷 통일

---

## 패키지 구조

```
src/main/java/shop/example/shop/
├── domain/
│   ├── member/   { entity, repository, service, controller, dto }
│   ├── item/     { entity(Category/Item/ItemOption), ... }
│   ├── cart/     { entity(Cart/CartItem), ... }
│   └── order/    { entity(Order/OrderItem), ... }
├── global/
│   ├── jwt/          # JWT 필터, 유틸
│   ├── exception/    # 전역 예외 처리
│   └── response/
└── config/           # Security, Swagger 설정
```

---

## API 명세 (총 19개)

| 메서드 | URL | 권한 | 설명 |
|---|---|---|---|
| POST | /api/auth/signup | - | 회원가입 |
| POST | /api/auth/login | - | 로그인 (JWT 발급) |
| GET | /api/members/me | USER | 내 정보 조회 |
| GET | /api/categories | - | 카테고리 목록 |
| POST | /api/categories | ADMIN | 카테고리 생성 |
| DELETE | /api/categories/{id} | ADMIN | 카테고리 삭제 |
| GET | /api/items | - | 상품 목록 (페이지네이션) |
| GET | /api/items/search | - | 상품 검색 |
| GET | /api/items/{id} | - | 상품 상세 |
| POST | /api/items | ADMIN | 상품 등록 |
| PUT | /api/items/{id} | ADMIN | 상품 수정 |
| DELETE | /api/items/{id} | ADMIN | 상품 삭제 |
| GET | /api/cart | USER | 장바구니 조회 |
| POST | /api/cart/items | USER | 담기 (중복 시 수량 합산) |
| PATCH | /api/cart/items/{id} | USER | 수량 변경 |
| DELETE | /api/cart/items/{id} | USER | 장바구니 삭제 |
| POST | /api/orders | USER | 장바구니 → 주문 |
| GET | /api/orders | USER | 주문 목록 |
| GET | /api/orders/{id} | USER | 주문 상세 |
| DELETE | /api/orders/{id} | USER | 주문 취소 (PENDING) |

---

## 리팩토링 성과

| 항목 | Before | After |
|---|---|---|
| DTO 변환 위치 | Service / Controller 혼재 | Service 일원화 |
| 장바구니/주문 조회 쿼리 | N+1 발생 | `@EntityGraph`로 단일 쿼리 |
| 재고 차감 로직 | 누락 | `decreaseStock` / `increaseStock` 적용 |
| 빈 장바구니 에러 | `CART_NOT_FOUND` (의미 불일치) | `CART_EMPTY` 신규 추가 |
| 상품 수정 상태 | 항상 `SELLING` 고정 (버그) | 요청값 반영, 미입력 시 기존 상태 유지 |
| 테스트 인프라 | 0% | H2 + JUnit 5 환경 구축 |

---

## 트러블슈팅

**1) ADMIN 계정인데 POST /api/items 403**
- 원인: `JwtAuthenticationFilter`가 토큰의 role 클레임을 읽지 않고 `ROLE_USER`로 고정
- 해결: 토큰 생성 시 `role` claim 포함 → 필터에서 `getRole()` 추출 → `SimpleGrantedAuthority("ROLE_" + role)` 매핑
- **학습**: 인증과 인가는 분리된 책임. 토큰 발급/파싱과 권한 매핑이 한 군데에 묶이면 디버깅이 어렵다.

**2) 상품 등록 후 `options: []` 반환**
- 원인: JPA 1차 캐시. 옵션 저장 후 같은 트랜잭션 내에서 item을 재조회하니 캐시된 객체(옵션 미포함) 반환
- 해결: 저장한 옵션을 `List<ItemOption> savedOptions`로 직접 수집해 DTO에 전달
- **학습**: 영속성 컨텍스트는 동일 트랜잭션의 동일 ID 조회 시 DB가 아닌 1차 캐시를 반환한다. "Save 후 다시 조회"는 무해해 보이지만 함정.

**3) GET /api/items 비로그인 시 403**
- 원인: SecurityConfig의 `"/api/items/**"` 패턴이 `/api/items/1`은 매칭하지만 `/api/items` (끝 슬래시 없는 경로)는 매칭하지 않음
- 해결: `"/api/items"` 와 `"/api/items/**"` 모두 명시적으로 permitAll에 추가
- **학습**: 보안 규칙은 "기본 차단 + 명시적 허용". Ant 패턴이 한 글자라도 어긋나면 그대로 차단된다.

**4) MySQL 8.x: Public Key Retrieval is not allowed**
- 원인: MySQL 8 기본 인증 `caching_sha2_password` — SSL 미사용 시 첫 연결에서 공개키 회수 차단
- 해결: JDBC URL에 `allowPublicKeyRetrieval=true` 추가 (개발 환경 한정)
- **학습**: 보안 옵션은 환경마다 트레이드오프가 다르다. 개발 편의 옵션을 운영에 그대로 들고 가면 평문 키 교환 위험이 생긴다.

---

## 백엔드 개발자 관점에서 다시 보면

> 취준하며 이 프로젝트를 회고했을 때 "지금 다시 짠다면" 어떻게 다르게 갈지 정리한 부분.

- **동시성 시나리오 대응 부족**: 재고 차감 로직은 단일 트랜잭션 정합성만 보장. 같은 옵션에 동시 주문이 들어오면 락이 없어 over-selling 가능. 비관적 락(`@Lock(PESSIMISTIC_WRITE)`) 또는 낙관적 락(`@Version`) 중 트래픽 특성에 맞춰 선택해야 한다.
- **결제 트랜잭션 경계**: 실제 e-커머스는 주문 생성과 PG 결제 요청이 별도 시스템에 걸쳐 있어 단일 `@Transactional`로 묶을 수 없다. 사가(Saga) 패턴 또는 이벤트 기반 보상 트랜잭션으로 풀어야 한다.
- **읽기/쓰기 분리**: 상품 목록 조회는 압도적 읽기 비중. Replica 라우팅 또는 Redis 캐시 레이어로 RDB 부하를 줄일 수 있다.
- **테스트 피라미드**: 컨텍스트 로딩 테스트만으로는 회귀 방지가 약하다. Service 단위 → Repository 슬라이스(`@DataJpaTest`) → 통합 테스트 순서로 구성하는 게 비용 대비 효과가 좋다.
- **관측 가능성**: 인프라 학습 경험과 연결한다면 `Micrometer + Prometheus`로 주문 성공률·평균 응답 시간을 메트릭화하는 것이 다음 단계 자연스러운 확장.

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
