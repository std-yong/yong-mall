package shop.example.shop.domain.order.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 회원의 주문 목록 조회 (페이지네이션)
    Page<Order> findByMemberId(Long memberId, Pageable pageable);
}
