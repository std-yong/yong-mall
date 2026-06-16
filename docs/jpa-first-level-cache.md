# JPA 1차 캐시, 나는 같은 트랜잭션 안에서 끝까지 따라가봤다

> Shopping Mall API 개인 프로젝트에서 만난 버그 하나를 끝까지 파보면서
> JPA의 영속성 컨텍스트(1차 캐시)가 실제로 어떻게 동작하는지 처음으로 손으로 확인했다.

---

## 1. 어떤 버그였나

ADMIN 계정으로 옵션이 있는 상품을 하나 등록했다.
요청은 200 OK로 잘 떨어졌다. 그런데 응답을 보니 이상했다.

```json
{
  "id": 14,
  "name": "오버사이즈 후드",
  "price": 49000,
  "options": []
}
```

분명히 옵션 두 개(`S`, `M`)를 같이 등록했는데 `options`가 빈 배열로 내려왔다.
DB를 확인해보니 옵션은 정상적으로 들어가 있었다.

> **저장은 됐는데, 응답이 비어 있다.**

이 한 문장이 이 글 전체의 출발점이다.

---

## 2. 처음 의심한 것 — 매핑이 잘못됐나?

가장 먼저 의심한 건 JPA 매핑이었다.
`Item`에 `@OneToMany(mappedBy = "item")`이 있고, `ItemOption`에 `@ManyToOne`이 있는 구조였다.

```java
@Entity
public class Item {
    @Id @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOption> options = new ArrayList<>();
}
```

매핑은 멀쩡했다. 그런데 왜 응답에는 옵션이 없을까?
서비스 코드를 다시 봤다.

```java
@Transactional
public ItemResponse createItem(ItemCreateRequest request) {
    Item item = itemRepository.save(request.toEntity());

    for (var optionRequest : request.getOptions()) {
        ItemOption option = ItemOption.of(item, optionRequest);
        itemOptionRepository.save(option);
    }

    Item saved = itemRepository.findById(item.getId())
            .orElseThrow();

    return ItemResponse.from(saved);  // ← 여기서 options가 비어있음
}
```

"분명히 옵션을 save 했고, 다시 findById로 꺼냈는데?"
이 시점에 처음으로 **1차 캐시**를 떠올렸다.

---

## 3. 1차 캐시가 도대체 뭔데

JPA를 배울 때 "영속성 컨텍스트", "1차 캐시"라는 단어를 분명히 들었다.
그런데 그게 "내 코드 위에서 실제로 어떻게 동작하는지"는 머리로만 알고 있었다.

정리하면 이렇다.

- **트랜잭션이 시작되면** Spring은 영속성 컨텍스트(EntityManager)를 하나 만든다.
- 이 컨텍스트는 **Map 같은 캐시**다. 키는 `(엔티티 타입, ID)`, 값은 엔티티 객체.
- `save()`, `find()` 같은 메서드를 호출하면 이 캐시에 객체가 등록된다.
- **같은 트랜잭션 안에서 같은 ID를 다시 조회하면, JPA는 DB를 보지 않고 캐시에 있는 그 객체를 그대로 돌려준다.**

여기까지 이해하고 내 코드를 다시 보니, 의심이 한 곳에 모였다.

> `itemRepository.save(item)`을 한 시점에 캐시에 들어간 `item`은 `options`가 빈 컬렉션이다.
> 그 뒤에 옵션을 따로 save 했지만, **그 옵션들이 캐시 안에 있는 `item.options` 리스트에 자동으로 추가되지는 않는다.**
> 그러면 `findById(item.getId())`는 캐시에서 빈 옵션을 가진 그 `item`을 그대로 돌려준다.

가설은 섰다. 이제 확인할 차례.

---

## 4. 진짜로 그런지 SQL 로그로 확인

`application.yml`에서 SQL 로그를 켰다.

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
logging:
  level:
    org.hibernate.SQL: debug
```

다시 요청을 보냈다. 로그를 보고 깜짝 놀랐다.

```
insert into item ...
insert into item_option ...
insert into item_option ...
-- findById 호출했는데 SELECT가 안 나감
```

`findById` 호출에서 SELECT 쿼리가 **아예 발생하지 않았다.**
캐시에 이미 같은 ID의 Item이 있으니, JPA는 DB를 조회하지 않고 캐시 객체를 그대로 돌려준 것이다.
가설이 맞았다.

---

## 5. 해결책 세 가지를 비교했다

### (1) `entityManager.flush()` + `clear()`로 강제 재조회

```java
em.flush();
em.clear();
Item saved = itemRepository.findById(item.getId()).orElseThrow();
```

- 1차 캐시를 비우니 `findById`가 진짜 SELECT를 날린다.
- 단점 : 컨텍스트를 통째로 비워서 다른 영속 객체까지 영향을 받고, 일반 비즈니스 로직에서 `clear()`를 쓰는 건 코드 냄새가 강하다.

### (2) `item.getOptions().add(option)` — 양방향 관계 동기화

```java
for (var optionRequest : request.getOptions()) {
    ItemOption option = ItemOption.of(item, optionRequest);
    item.getOptions().add(option);     // 양쪽 다 채워준다
    itemOptionRepository.save(option);
}
```

- 캐시에 있는 그 `item` 객체의 컬렉션에 직접 옵션을 넣어주는 방법.
- JPA 양방향 관계에서 흔히 쓰는 패턴이다.
- 단점 : `save()`와 `add()`를 둘 다 호출하는 게 일관성이 떨어진다고 느꼈다. cascade를 쓰면 깔끔하지만 그건 또 다른 설계 결정이 필요했다.

### (3) DTO를 직접 만든다 — 재조회 자체를 없앤다 ✅ 선택

```java
@Transactional
public ItemResponse createItem(ItemCreateRequest request) {
    Item item = itemRepository.save(request.toEntity());

    List<ItemOption> savedOptions = request.getOptions().stream()
            .map(req -> ItemOption.of(item, req))
            .map(itemOptionRepository::save)
            .toList();

    return ItemResponse.of(item, savedOptions);
}
```

- 어차피 응답에 필요한 데이터는 **방금 내가 만든 것**이다.
- 그걸 그대로 DTO에 담으면 재조회 자체가 없어진다.
- 1차 캐시 문제는 발생할 일이 없다.

세 번째를 선택했다.

> "재조회를 해서 최신 상태를 얻는다"라는 발상 자체가 어색했다.
> 내가 방금 save한 객체와 옵션 리스트가 손에 있는데 굳이 다시 DB(혹은 캐시)에서 꺼낼 이유가 없었다.

---

## 6. 내가 진짜로 배운 것

처음에는 단순한 "응답 DTO 버그"로 보였다. 끝까지 따라가보니 세 가지가 남았다.

**첫 번째.** 같은 트랜잭션 안에서 같은 ID를 조회하면, JPA는 DB가 아니라 영속성 컨텍스트를 본다.
"저장 후 재조회 = 최신 상태"라는 직관은 **JPA에서는 틀릴 수 있다.**

**두 번째.** SQL 로그를 켜야 진짜로 무슨 일이 일어나는지 보인다.
이전까지는 SQL 로그를 "쿼리 잘 나가나 확인하는 용도"로만 썼는데, 이 버그를 통해 **"안 나간 쿼리"가 더 중요한 단서**라는 걸 알았다.

**세 번째.** 가장 좋은 코드는 문제 자체가 생기지 않는 코드다.
재조회를 영리하게 우회하는 방법보다, **재조회를 하지 않는 흐름**이 더 명확했다.

---

## 7. 만약 다시 본다면

지금 이 코드를 다시 본다면 한 단계 더 갈 것 같다.

- `ItemRepository`에서 `cascade = CascadeType.ALL`을 활용해 옵션을 같이 저장하는 방법
- 옵션 ID 생성을 DB에 위임하지 않고 도메인에서 부여하는 구조

다만 그건 "옵션이 Item에 종속적인 값인가, 아니면 독립 도메인인가"를 먼저 정리한 다음이다.
JPA의 편의 기능보다 **도메인 경계가 먼저**라는 점은, 이 버그 이후로 계속 의식하고 있는 부분이다.

---

### 참고

- **프로젝트** : Shopping Mall API (Spring Boot 3.5, JPA, MySQL 8)
- **관련 코드** : `shop/src/main/java/shop/example/shop/domain/item/service/ItemService.java`
