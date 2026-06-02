# 🛍️ Yong-Mall API

Spring Boot 기반 의류 쇼핑몰 REST API 서버입니다

회원가입, 로그인, 상품 관리, 장바구니, 주문 흐름을 구현하면서
JPA 연관관계, JWT 인증/인가, 트랜잭션 처리, 재고 차감 로직을 직접 다뤄보기 위해 만든 개인 프로젝트입니다

특히 단순 CRUD에서 끝내지 않고, 개발 중 만난 버그를 수정하면서
JPA 1차 캐시, N+1 문제, Security 권한 처리, 주문 시점 가격 저장 같은 백엔드 구현 이슈를 정리해보는 데 중점을 두었습니다

---

## 기술 스택

| 분류        | 기술                                                               |
| --------- | ---------------------------------------------------------------- |
| Language  | Java 21                                                          |
| Framework | Spring Boot 3.5.13, Spring Web, Spring Data JPA, Spring Security |
| ORM / DB  | Hibernate, MySQL 8.0, H2                                         |
| 인증/인가     | JWT, BCrypt                                                      |
| API 문서    | SpringDoc OpenAPI                                                |
| 빌드 / 테스트  | Gradle, JUnit 5                                                  |

---

## 프로젝트에서 신경 쓴 부분

### 1. 도메인 분리

회원, 상품, 장바구니, 주문 도메인을 나누고
각 도메인별로 `controller`, `service`, `repository`, `dto`, `entity` 구조를 정리했습니다

처음에는 기능 구현에만 집중했지만, 리팩토링하면서 DTO 변환 위치와 비즈니스 로직 위치가 섞이지 않도록 정리했습니다

---

### 2. JWT 기반 인증/인가

회원가입 시 비밀번호는 BCrypt로 암호화하고, 로그인 성공 시 JWT를 발급하도록 구현했습니다

ADMIN 권한이 필요한 상품 등록, 수정, 삭제 API와
USER 권한이 필요한 장바구니, 주문 API를 분리했습니다

개발 중 ADMIN 계정으로 상품 등록 요청을 보냈는데 403이 발생한 문제가 있었고,
JWT 필터에서 토큰의 role 값을 제대로 읽지 않고 항상 `ROLE_USER`로 처리하고 있던 것이 원인이었습니다

이 문제를 수정하면서 인증과 인가가 코드상에서 어디서 나뉘는지 더 명확히 이해할 수 있었습니다

---

### 3. 상품 옵션과 재고 관리

상품은 여러 개의 옵션을 가질 수 있도록 구성했습니다

예를 들어 같은 후드티라도 사이즈나 색상에 따라 각각 다른 재고를 갖도록 했습니다

주문 생성 시에는 선택한 옵션의 재고를 차감하고,
주문 취소 시에는 차감했던 재고를 다시 복구하도록 구현했습니다

현재는 단일 트랜잭션 안에서의 정합성만 처리되어 있어
동시에 같은 상품 옵션을 주문하는 상황에서는 추가적인 락 처리가 필요합니다

이 부분은 이후 비관적 락이나 낙관적 락을 적용해 개선해보고 싶은 지점입니다

---

### 4. 주문 시점 가격 저장

주문 생성 시 상품 가격을 그대로 참조하지 않고
주문 당시의 가격을 `OrderItem`에 따로 저장했습니다

상품 가격은 이후 변경될 수 있기 때문에,
과거 주문 내역은 주문 당시 가격을 기준으로 유지되어야 한다고 판단했습니다

---

### 5. JPA 조회 최적화

장바구니와 주문 조회 과정에서 연관된 엔티티를 반복 조회하면서 N+1 문제가 발생할 수 있는 구조였습니다

이를 개선하기 위해 필요한 조회에는 `@EntityGraph`를 적용해
장바구니, 주문, 주문상품, 상품 옵션 정보를 함께 조회하도록 수정했습니다

---

## 주요 기능

### 회원

* 회원가입
* 로그인
* JWT 발급
* 내 정보 조회
* 가입 시 장바구니 자동 생성

### 상품 / 카테고리

* 카테고리 등록, 조회, 삭제
* 상품 등록, 조회, 수정, 삭제
* 상품 옵션 등록
* 상품 검색
* 상품 목록 페이지네이션
* ADMIN 권한 기반 상품 관리

### 장바구니

* 회원별 장바구니 조회
* 상품 옵션 담기
* 같은 옵션을 다시 담는 경우 수량 합산
* 장바구니 수량 변경
* 장바구니 상품 삭제

### 주문

* 장바구니 상품을 주문으로 변환
* 주문 시점 가격 저장
* 주문 생성 시 재고 차감
* 주문 취소 시 재고 복구
* PENDING 상태 주문만 취소 가능
* 주문 목록 및 상세 조회

### 예외 처리

* `CustomException`
* `ErrorCode`
* `@RestControllerAdvice`

공통 예외 응답 구조를 만들어 API 에러 응답 형식을 통일했습니다

---

## 패키지 구조

```text
src/main/java/shop/example/shop/
├── domain/
│   ├── member/
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── dto
│   │   └── entity
│   ├── item/
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── dto
│   │   └── entity
│   ├── cart/
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── dto
│   │   └── entity
│   └── order/
│       ├── controller
│       ├── service
│       ├── repository
│       ├── dto
│       └── entity
├── global/
│   ├── jwt
│   ├── exception
│   └── response
└── config/
```

---

## API 명세

총 20개의 API를 구현했습니다

| 메서드    | URL                    | 권한    | 설명            |
| ------ | ---------------------- | ----- | ------------- |
| POST   | `/api/auth/signup`     | -     | 회원가입          |
| POST   | `/api/auth/login`      | -     | 로그인           |
| GET    | `/api/members/me`      | USER  | 내 정보 조회       |
| GET    | `/api/categories`      | -     | 카테고리 목록 조회    |
| POST   | `/api/categories`      | ADMIN | 카테고리 생성       |
| DELETE | `/api/categories/{id}` | ADMIN | 카테고리 삭제       |
| GET    | `/api/items`           | -     | 상품 목록 조회      |
| GET    | `/api/items/search`    | -     | 상품 검색         |
| GET    | `/api/items/{id}`      | -     | 상품 상세 조회      |
| POST   | `/api/items`           | ADMIN | 상품 등록         |
| PUT    | `/api/items/{id}`      | ADMIN | 상품 수정         |
| DELETE | `/api/items/{id}`      | ADMIN | 상품 삭제         |
| GET    | `/api/cart`            | USER  | 장바구니 조회       |
| POST   | `/api/cart/items`      | USER  | 장바구니 상품 담기    |
| PATCH  | `/api/cart/items/{id}` | USER  | 장바구니 상품 수량 변경 |
| DELETE | `/api/cart/items/{id}` | USER  | 장바구니 상품 삭제    |
| POST   | `/api/orders`          | USER  | 주문 생성         |
| GET    | `/api/orders`          | USER  | 주문 목록 조회      |
| GET    | `/api/orders/{id}`     | USER  | 주문 상세 조회      |
| DELETE | `/api/orders/{id}`     | USER  | 주문 취소         |

---

## 리팩토링 내역

| 항목           | 기존                      | 수정 후                 |
| ------------ | ----------------------- | -------------------- |
| DTO 변환 위치    | Service와 Controller에 혼재 | Service 중심으로 정리      |
| 장바구니 / 주문 조회 | 연관 엔티티 조회 시 N+1 가능성     | `@EntityGraph` 적용    |
| 재고 처리        | 주문 생성 시 재고 차감 누락        | 차감 / 복구 메서드 추가       |
| 빈 장바구니 예외    | `CART_NOT_FOUND` 사용     | `CART_EMPTY` 예외 추가   |
| 상품 수정        | 상태값이 항상 `SELLING`으로 고정  | 요청값 반영, 미입력 시 기존값 유지 |
| 테스트 환경       | 테스트 설정 없음               | H2 기반 테스트 환경 구성      |

---

## 트러블슈팅

### 1. ADMIN 계정인데 상품 등록 API에서 403 발생

#### 문제

ADMIN 계정으로 로그인한 뒤 상품 등록 API를 호출했지만 403 응답이 발생했습니다

#### 원인

JWT 필터에서 토큰의 role 값을 읽지 않고
인증 객체를 만들 때 항상 `ROLE_USER`로 고정하고 있었습니다

#### 해결

토큰 생성 시 role claim을 포함하고,
필터에서 해당 값을 추출해 `SimpleGrantedAuthority("ROLE_" + role)` 형태로 권한을 매핑했습니다

#### 배운 점

로그인에 성공했다고 해서 인가까지 자동으로 처리되는 것은 아니었습니다

JWT 발급, 토큰 파싱, SecurityContext 등록, 권한 매핑 흐름이 모두 맞아야
권한 기반 API 접근이 정상적으로 동작한다는 것을 확인했습니다

---

### 2. 상품 등록 후 응답에서 options가 빈 배열로 내려오는 문제

#### 문제

옵션이 있는 상품을 등록했는데 응답 DTO에서는 `options: []`로 내려왔습니다

DB에는 옵션 데이터가 정상적으로 저장되어 있었습니다

#### 원인

상품 저장 후 같은 트랜잭션 안에서 상품을 다시 조회했지만,
JPA 영속성 컨텍스트의 1차 캐시에 저장된 기존 상품 객체가 반환되었습니다

이 객체에는 방금 저장한 옵션 목록이 반영되어 있지 않았습니다

#### 해결

옵션 저장 시 `savedOptions` 리스트를 직접 수집하고,
응답 DTO 생성 시 해당 리스트를 사용하도록 수정했습니다

#### 배운 점

같은 트랜잭션 안에서 같은 ID를 다시 조회하면 DB를 다시 조회하는 것이 아니라
영속성 컨텍스트의 1차 캐시 객체가 반환될 수 있다는 점을 확인했습니다

---

### 3. 비로그인 상태에서 상품 목록 조회 시 403 발생

#### 문제

상품 목록 조회 API는 비로그인 사용자도 접근 가능해야 했지만
`GET /api/items` 요청에서 403이 발생했습니다

#### 원인

Security 설정에서 `"/api/items/**"`만 permitAll로 열어두었습니다

이 패턴은 `/api/items/1` 같은 하위 경로에는 적용되지만
`/api/items` 자체에는 적용되지 않았습니다

#### 해결

`"/api/items"`와 `"/api/items/**"`를 모두 permitAll에 추가했습니다

#### 배운 점

보안 설정은 기본적으로 막고 필요한 경로만 여는 방식이기 때문에
경로 패턴 하나가 어긋나도 의도와 다르게 차단될 수 있다는 점을 알게 되었습니다

---

### 4. MySQL 8 연결 시 Public Key Retrieval is not allowed 발생

#### 문제

로컬 환경에서 MySQL 8에 연결할 때
`Public Key Retrieval is not allowed` 오류가 발생했습니다

#### 원인

MySQL 8의 기본 인증 방식인 `caching_sha2_password`와 관련된 문제였습니다

#### 해결

개발 환경의 JDBC URL에 `allowPublicKeyRetrieval=true` 옵션을 추가해 해결했습니다

#### 배운 점

개발 편의를 위한 DB 연결 옵션이 운영 환경에서도 그대로 안전한 것은 아니기 때문에
환경별 설정을 분리해서 관리해야 한다고 느꼈습니다

---

## 아쉬운 점과 개선하고 싶은 부분

### 1. 동시성 처리

현재 재고 차감은 단일 요청 기준으로는 동작하지만
여러 사용자가 동시에 같은 옵션을 주문하는 상황까지는 충분히 처리하지 못합니다

이후에는 `@Lock(PESSIMISTIC_WRITE)`를 사용한 비관적 락이나
`@Version`을 사용한 낙관적 락을 적용해보고 싶습니다

---

### 2. 결제 흐름 미구현

현재 프로젝트는 주문 생성까지만 구현되어 있고
실제 결제 승인, 결제 실패, 주문 보상 처리 흐름은 포함되어 있지 않습니다

실제 서비스라면 주문 생성과 결제 처리를 하나의 트랜잭션으로만 묶기 어렵기 때문에
결제 실패 시 주문 상태 변경이나 재고 복구 흐름을 별도로 설계해야 할 것 같습니다

---

### 3. 테스트 보강 필요

현재는 테스트 환경을 구성한 단계에 가깝고
핵심 비즈니스 로직을 충분히 검증했다고 보기는 어렵습니다

이후에는 다음 순서로 테스트를 보강하고 싶습니다

* Service 단위 테스트
* Repository 슬라이스 테스트
* 주문 생성 / 취소 통합 테스트
* 인증 / 인가 API 테스트

---

### 4. 운영 관점 보강

인프라 학습 경험과 연결한다면
이 프로젝트도 단순 로컬 실행에서 끝내지 않고 운영 관점으로 확장해보고 싶습니다

예를 들어 주문 성공률, API 응답 시간, 에러율 같은 지표를 수집하면
백엔드 API가 실제로 어떻게 동작하는지 더 잘 확인할 수 있을 것 같습니다

---

## 실행 방법

### 1. 설정 파일 생성

`src/main/resources/application-local.properties` 파일을 생성합니다

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shop
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

jwt.secret=your_jwt_secret
```

### 2. 실행

```bash
./gradlew bootRun
```

### 3. Swagger 접속

```text
http://localhost:8080/swagger-ui.html
```

---

## 정리

이 프로젝트는 쇼핑몰 API라는 익숙한 주제를 통해
Spring Boot 백엔드 개발에서 자주 만나는 흐름을 직접 구현해본 프로젝트입니다

기능 자체는 기본적인 쇼핑몰 API에 가깝지만,
구현 과정에서 만난 문제를 수정하면서 JPA, Security, 트랜잭션, 예외 처리에 대한 이해를 조금 더 구체화할 수 있었습니다

아직 동시성 처리, 결제 흐름, 테스트 보강 같은 개선점이 남아 있지만
백엔드 개발자로 성장하기 위해 어떤 부분을 더 공부해야 하는지 확인할 수 있었던 프로젝트입니다
