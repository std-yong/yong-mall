package shop.example.shop.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.example.shop.domain.cart.entity.Cart;
import shop.example.shop.domain.cart.entity.CartItem;
import shop.example.shop.domain.cart.repository.CartRepository;
import shop.example.shop.domain.member.entity.Member;
import shop.example.shop.domain.member.repository.MemberRepository;
import shop.example.shop.domain.order.dto.OrderResponse;
import shop.example.shop.domain.order.entity.Order;
import shop.example.shop.domain.order.entity.OrderItem;
import shop.example.shop.domain.order.repository.OrderRepository;
import shop.example.shop.global.exception.CustomException;
import shop.example.shop.global.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public OrderResponse createOrder(Long memberId, String deliveryAddress) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));

        if (cart.getCartItems().isEmpty()) {
            throw new CustomException(ErrorCode.CART_EMPTY);
        }

        int totalPrice = cart.getCartItems().stream()
                .mapToInt(ci -> ci.getItemOption().getItem().getPrice() * ci.getQuantity())
                .sum();

        Order order = Order.builder()
                .member(member)
                .totalPrice(totalPrice)
                .deliveryAddress(deliveryAddress)
                .build();

        for (CartItem cartItem : cart.getCartItems()) {
            try {
                cartItem.getItemOption().decreaseStock(cartItem.getQuantity());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.OUT_OF_STOCK);
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .itemOption(cartItem.getItemOption())
                    .quantity(cartItem.getQuantity())
                    .orderPrice(cartItem.getItemOption().getItem().getPrice())
                    .build();
            order.getOrderItems().add(orderItem);
        }

        orderRepository.save(order);

        cart.getCartItems().clear();

        return new OrderResponse(order);
    }

    public Page<OrderResponse> getOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable)
                .map(OrderResponse::new);
    }

    public OrderResponse getOrderDetail(Long memberId, Long orderId) {
        Order order = getOrderByIdAndMemberId(memberId, orderId);
        return new OrderResponse(order);
    }

    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {
        Order order = getOrderByIdAndMemberId(memberId, orderId);

        for (OrderItem orderItem : order.getOrderItems()) {
            orderItem.getItemOption().increaseStock(orderItem.getQuantity());
        }

        order.cancel();
    }

    private Order getOrderByIdAndMemberId(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return order;
    }
}