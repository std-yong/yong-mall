
package shop.example.shop.domain.cart.dto;
import lombok.Getter;
import shop.example.shop.domain.cart.entity.CartItem;
import shop.example.shop.domain.item.entity.ItemOption;

@Getter
public class CartItemResponse {

    // 여기에 필드 선언해봐요
    // 우리가 필요하다고 했던 것들: 상품명, 사이즈, 색상, 단가, 수량, 소계
    private final Long cartItemId;
    private final String itemName;
    private final String size;
    private final String color;
    private final int price;
    private final int quantity;
    private final int subtotal; // price * quantity

    public CartItemResponse(CartItem cartItem) {
        ItemOption itemOption = cartItem.getItemOption();
        this.cartItemId = cartItem.getId();
        this.itemName = itemOption.getItem().getName();
        this.size = itemOption.getSize().name();
        this.color = itemOption.getColor();
        this.price = itemOption.getItem().getPrice();
        this.quantity = cartItem.getQuantity();
        this.subtotal = this.price * this.quantity;
    }
}