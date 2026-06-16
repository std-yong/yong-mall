package shop.example.shop.domain.cart.repository;

import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import shop.example.shop.domain.cart.entity.Cart;
import shop.example.shop.domain.cart.entity.CartItem;
import shop.example.shop.domain.item.entity.Category;
import shop.example.shop.domain.item.entity.Item;
import shop.example.shop.domain.item.entity.ItemOption;
import shop.example.shop.domain.member.entity.Member;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CartRepository.findByMemberId 의 @EntityGraph 가 실제로 N+1 을 제거하는지
 * Hibernate Statistics 로 쿼리 카운트를 측정해 비교한다.
 *
 * Before: findById + lazy 접근 → 카트 + cartItems + N*(itemOption + item)
 * After:  findByMemberId (@EntityGraph) → 단일 join fetch
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("CartRepository N+1 측정")
class CartRepositoryNPlusOneTest {

    private static final int ITEM_COUNT = 10;

    @Autowired CartRepository cartRepository;
    @Autowired EntityManager em;

    private Long memberId;
    private Long cartId;
    private Statistics stats;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .email("nplus1@test.com").password("x").name("tester").role(Member.Role.USER).build();
        em.persist(member);

        Category category = new Category("top");
        em.persist(category);

        Cart cart = new Cart(member);
        em.persist(cart);

        for (int i = 0; i < ITEM_COUNT; i++) {
            Item item = Item.builder()
                    .category(category).name("item-" + i).description("d").price(10000).build();
            em.persist(item);

            ItemOption opt = ItemOption.builder()
                    .item(item).size(ItemOption.Size.M).color("blue").stockQuantity(100).build();
            em.persist(opt);

            CartItem ci = CartItem.builder().cart(cart).itemOption(opt).quantity(1).build();
            em.persist(ci);
        }
        em.flush();
        em.clear();

        memberId = member.getId();
        cartId = cart.getId();

        stats = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();
    }

    @Test
    @DisplayName("Before — findById + lazy 그래프 순회는 N+1 을 일으킨다")
    void findById_lazy_causesNPlusOne() {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        cart.getCartItems().forEach(ci ->
                ci.getItemOption().getItem().getName()
        );

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("[Before/N+1] prepare statement count = " + queryCount + " (ITEM_COUNT=" + ITEM_COUNT + ")");

        // 1(cart) + 1(cartItems collection) + N(itemOption) + N(item) = 최소 2 + 2N
        assertThat(queryCount).isGreaterThanOrEqualTo(2L * ITEM_COUNT);
    }

    @Test
    @DisplayName("After — @EntityGraph 가 적용된 findByMemberId 는 단일 쿼리로 끝낸다")
    void findByMemberId_entityGraph_singleQuery() {
        Cart cart = cartRepository.findByMemberId(memberId).orElseThrow();
        cart.getCartItems().forEach(ci ->
                ci.getItemOption().getItem().getName()
        );

        long queryCount = stats.getPrepareStatementCount();
        System.out.println("[After/EntityGraph] prepare statement count = " + queryCount + " (ITEM_COUNT=" + ITEM_COUNT + ")");

        assertThat(queryCount).isEqualTo(1L);
    }
}
