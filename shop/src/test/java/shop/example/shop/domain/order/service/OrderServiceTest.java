package shop.example.shop.domain.order.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import shop.example.shop.domain.cart.entity.Cart;
import shop.example.shop.domain.cart.entity.CartItem;
import shop.example.shop.domain.cart.repository.CartRepository;
import shop.example.shop.domain.item.entity.Item;
import shop.example.shop.domain.item.entity.ItemOption;
import shop.example.shop.domain.member.entity.Member;
import shop.example.shop.domain.member.repository.MemberRepository;
import shop.example.shop.domain.order.entity.Order;
import shop.example.shop.domain.order.repository.OrderRepository;
import shop.example.shop.global.exception.CustomException;
import shop.example.shop.global.exception.ErrorCode;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_빈_장바구니면_예외_발생() {
        // given
        Long memberId = 1L;

        Member member = Member.builder().email("test@test.com").password("pw").name("유저").build();
        Cart emptyCart = new Cart(member); // cartItems가 빈 리스트

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(emptyCart));

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> orderService.createOrder(memberId, "서울시 강남구"));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CART_EMPTY);
    }

    @Test
    void createOrder_재고_부족하면_예외_발생() {
        // given
        Long memberId = 1L;

        Member member = Member.builder().email("test@test.com").password("pw").name("유저").build();
        Cart cart = new Cart(member);

        // 재고가 1개뿐인데 2개 담은 상황
        Item item = Item.builder().name("티셔츠").price(20000).build();
        ItemOption option = ItemOption.builder()
                .item(item)
                .size(ItemOption.Size.M)
                .color("화이트")
                .stockQuantity(1)
                .build();
        CartItem cartItem = new CartItem(cart, option, 2);

        // Cart에 CartItem 직접 주입
        ReflectionTestUtils.setField(cart, "cartItems", List.of(cartItem));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> orderService.createOrder(memberId, "서울시 강남구"));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
    }

    @Test
    void cancelOrder_PENDING_아닌_주문은_취소_불가() {
        // given
        Long memberId = 1L;
        Long orderId = 100L;

        Member member = Member.builder().email("test@test.com").password("pw").name("유저").build();
        ReflectionTestUtils.setField(member, "id", memberId);

        // PENDING이 아닌 SHIPPING 상태의 주문
        Order order = Order.builder()
                .member(member)
                .totalPrice(20000)
                .deliveryAddress("서울시 강남구")
                .build();
        order.updateStatus(Order.Status.SHIPPING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> orderService.cancelOrder(memberId, orderId));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }
}
