package shop.example.shop.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import shop.example.shop.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")   // order는 MySQL 예약어라 orders 사용
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status;

    @Column(nullable = false, length = 255)
    private String deliveryAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        PENDING, PAID, SHIPPING, DELIVERED, CANCELLED
    }

    @Builder
    public Order(Member member, int totalPrice, String deliveryAddress) {
        this.member = member;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
        this.status = Status.PENDING;
    }

    // 주문 취소 (PENDING 상태일 때만 가능)
    public void cancel() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("이미 처리된 주문은 취소할 수 없습니다.");
        }
        this.status = Status.CANCELLED;
    }

    // 주문 상태 변경 (관리자용)
    public void updateStatus(Status status) {
        this.status = status;
    }
}
