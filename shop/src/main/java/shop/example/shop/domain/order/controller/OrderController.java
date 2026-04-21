package shop.example.shop.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import shop.example.shop.domain.order.dto.CreateOrderRequest;
import shop.example.shop.domain.order.dto.OrderResponse;
import shop.example.shop.domain.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders - 주문 생성
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = new OrderResponse(
                orderService.createOrder(memberId, request.getDeliveryAddress())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/orders - 내 주문 목록
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @AuthenticationPrincipal Long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OrderResponse> response = orderService.getOrders(memberId, pageable)
                .map(OrderResponse::new);
        return ResponseEntity.ok(response);
    }

    // GET /api/orders/{orderId} - 주문 상세
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId
    ) {
        OrderResponse response = new OrderResponse(
                orderService.getOrderDetail(memberId, orderId)
        );
        return ResponseEntity.ok(response);
    }

    // DELETE /api/orders/{orderId} - 주문 취소
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId
    ) {
        orderService.cancelOrder(memberId, orderId);
        return ResponseEntity.noContent().build();
    }
}