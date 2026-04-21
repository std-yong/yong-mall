package shop.example.shop.domain.order.dto;

import lombok.Getter;
import shop.example.shop.domain.order.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderResponse {

    private final Long orderId;
    private final String status;
    private final int totalPrice;
    private final String deliveryAddress;
    private final LocalDateTime createdAt;
    private final List<OrderItemResponse> orderItems;

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.status = order.getStatus().name();
        this.totalPrice = order.getTotalPrice();
        this.deliveryAddress = order.getDeliveryAddress();
        this.createdAt = order.getCreatedAt();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemResponse::new)
                .toList();
    }
}