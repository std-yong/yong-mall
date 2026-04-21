package shop.example.shop.domain.order.dto;

import lombok.Getter;
import shop.example.shop.domain.order.entity.OrderItem;

@Getter
public class OrderItemResponse {

    private final Long orderItemId;
    private final String itemName;
    private final String size;
    private final String color;
    private final int orderPrice;
    private final int quantity;
    private final int subtotal;

    public OrderItemResponse(OrderItem orderItem) {
        this.orderItemId = orderItem.getId();
        this.itemName = orderItem.getItemOption().getItem().getName();
        this.size = orderItem.getItemOption().getSize().name();
        this.color = orderItem.getItemOption().getColor();
        this.orderPrice = orderItem.getOrderPrice();
        this.quantity = orderItem.getQuantity();
        this.subtotal = orderItem.getSubtotal();
    }
}
