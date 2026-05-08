package shop.example.shop.domain.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderItems", "orderItems.itemOption", "orderItems.itemOption.item"})
    Page<Order> findByMemberId(Long memberId, Pageable pageable);
}
