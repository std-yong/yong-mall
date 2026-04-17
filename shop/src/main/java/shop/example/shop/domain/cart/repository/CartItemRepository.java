package shop.example.shop.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.cart.entity.CartItem;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 장바구니 안에 이미 같은 옵션이 담겨있는지 확인
    // (같은 옵션 또 담으면 수량만 추가)
    Optional<CartItem> findByCartIdAndItemOptionId(Long cartId, Long itemOptionId);
}
