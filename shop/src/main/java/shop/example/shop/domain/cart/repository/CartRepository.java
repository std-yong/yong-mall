package shop.example.shop.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.cart.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // 회원 ID로 장바구니 조회
    Optional<Cart> findByMemberId(Long memberId);
}
