package shop.example.shop.domain.cart.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.example.shop.domain.item.entity.ItemOption;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_option_id", nullable = false)
    private ItemOption itemOption;

    @Column(nullable = false)
    private int quantity;

    @Builder
    public CartItem(Cart cart, ItemOption itemOption, int quantity) {
        this.cart = cart;
        this.itemOption = itemOption;
        this.quantity = quantity;
    }

    // 수량 변경
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    // 수량 추가 (이미 담긴 옵션을 또 담을 때)
    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }
}
