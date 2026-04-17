package shop.example.shop.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.order.entity.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 주문에 포함된 상품 목록 조회
    List<OrderItem> findByOrderId(Long orderId);
}
