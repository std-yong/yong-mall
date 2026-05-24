package shop.example.shop.domain.cart.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import shop.example.shop.domain.cart.entity.Cart;
import shop.example.shop.domain.cart.entity.CartItem;
import shop.example.shop.domain.cart.repository.CartItemRepository;
import shop.example.shop.domain.cart.repository.CartRepository;
import shop.example.shop.domain.item.entity.Item;
import shop.example.shop.domain.item.entity.ItemOption;
import shop.example.shop.domain.item.repository.ItemOptionRepository;
import shop.example.shop.domain.member.entity.Member;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemOptionRepository itemOptionRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItemToCart_새상품이면_CartItem_저장() {
        // given
        Long memberId = 1L;
        Long itemOptionId = 10L;
        int quantity = 2;

        Member member = Member.builder().email("test@test.com").password("pw").name("유저").build();
        Cart cart = new Cart(member);
        ReflectionTestUtils.setField(cart, "id", 1L);

        Item item = Item.builder().name("티셔츠").price(20000).build();
        ItemOption itemOption = ItemOption.builder()
                .item(item)
                .size(ItemOption.Size.M)
                .color("블랙")
                .stockQuantity(10)
                .build();
        ReflectionTestUtils.setField(itemOption, "id", itemOptionId);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));
        when(itemOptionRepository.findById(itemOptionId)).thenReturn(Optional.of(itemOption));
        when(cartItemRepository.findByCartIdAndItemOptionId(1L, itemOptionId)).thenReturn(Optional.empty());

        // when
        cartService.addItemToCart(memberId, itemOptionId, quantity);

        // then
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_이미_담긴_옵션이면_수량_합산() {
        // given
        Long memberId = 1L;
        Long itemOptionId = 10L;
        int addQuantity = 3;

        Member member = Member.builder().email("test@test.com").password("pw").name("유저").build();
        Cart cart = new Cart(member);
        ReflectionTestUtils.setField(cart, "id", 1L);

        Item item = Item.builder().name("티셔츠").price(20000).build();
        ItemOption itemOption = ItemOption.builder()
                .item(item)
                .size(ItemOption.Size.M)
                .color("블랙")
                .stockQuantity(10)
                .build();

        // 이미 수량 2개가 담겨 있는 상태
        CartItem existingCartItem = new CartItem(cart, itemOption, 2);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));
        when(itemOptionRepository.findById(itemOptionId)).thenReturn(Optional.of(itemOption));
        when(cartItemRepository.findByCartIdAndItemOptionId(1L, itemOptionId))
                .thenReturn(Optional.of(existingCartItem));

        // when
        cartService.addItemToCart(memberId, itemOptionId, addQuantity);

        // then: 새로 저장하지 않고 기존 항목의 수량만 3 증가 → 총 5개
        verify(cartItemRepository).findByCartIdAndItemOptionId(1L, itemOptionId);
        org.assertj.core.api.Assertions.assertThat(existingCartItem.getQuantity()).isEqualTo(5);
    }
}
