package shop.example.shop.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.example.shop.domain.item.entity.ItemOption;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_option_id", nullable = false)
    private ItemOption itemOption;

    @Column(nullable = false)
    private int quantity;

    // 주문 당시 가격 스냅샷 (나중에 상품 가격이 바뀌어도 주문 내역은 유지됨)
    @Column(nullable = false)
    private int orderPrice;

    @Builder
    public OrderItem(Order order, ItemOption itemOption, int quantity, int orderPrice) {
        this.order = order;
        this.itemOption = itemOption;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
    }

    // 소계 계산
    public int getSubtotal() {
        return this.orderPrice * this.quantity;
    }
}
