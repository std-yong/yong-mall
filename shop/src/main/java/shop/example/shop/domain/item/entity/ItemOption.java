package shop.example.shop.domain.item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Size size;

    @Column(nullable = false, length = 30)
    private String color;

    @Column(nullable = false)
    private int stockQuantity;

    public enum Size {
        S, M, L, XL
    }

    @Builder
    public ItemOption(Item item, Size size, String color, int stockQuantity) {
        this.item = item;
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
    }

    // 재고 증가 (주문 취소 시 사용)
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void update(Size size, String color, int stockQuantity) {
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity;
    }
}
