package shop.example.shop.domain.cart.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import shop.example.shop.domain.cart.entity.Cart;
import shop.example.shop.domain.cart.entity.CartItem;
import shop.example.shop.domain.cart.repository.CartRepository;
import shop.example.shop.domain.cart.repository.CartItemRepository;
import shop.example.shop.domain.item.entity.ItemOption;
import shop.example.shop.domain.item.repository.ItemOptionRepository;
import shop.example.shop.global.exception.CustomException;
import shop.example.shop.global.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemOptionRepository itemOptionRepository;

    public Cart getCart(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
    }

    @Transactional
    public void addItemToCart(Long memberId, Long itemOptionId, int quantity) {
        Cart cart = getCart(memberId);
        ItemOption itemOption = itemOptionRepository.findById(itemOptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_OPTION_NOT_FOUND));

        Optional<CartItem> existing = cartItemRepository.findByCartIdAndItemOptionId(cart.getId(), itemOptionId);
        if (existing.isPresent()) {
            existing.get().addQuantity(quantity);
        } else {
            CartItem cartItem = new CartItem(cart, itemOption, quantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void updateItemQuantity(Long memberId, Long itemOptionId, int quantity) {
        Cart cart = getCart(memberId);
        CartItem cartItem = cartItemRepository.findByCartIdAndItemOptionId(cart.getId(), itemOptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.updateQuantity(quantity);
    }

    @Transactional
    public void deleteItemFromCart(Long memberId, Long itemOptionId) {
        Cart cart = getCart(memberId);
        CartItem cartItem = cartItemRepository.findByCartIdAndItemOptionId(cart.getId(), itemOptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartItemRepository.delete(cartItem);
    }

    public List<CartItem> getCartItems(Long memberId) {
        Cart cart = getCart(memberId);
        return cart.getCartItems();
    }
}
