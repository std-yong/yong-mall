package shop.example.shop.domain.cart.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.cart.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"cartItems", "cartItems.itemOption", "cartItems.itemOption.item"})
    Optional<Cart> findByMemberId(Long memberId);
}
