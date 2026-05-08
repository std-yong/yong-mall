package shop.example.shop.domain.cart.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import shop.example.shop.domain.cart.dto.CartAddRequest;
import shop.example.shop.domain.cart.dto.CartItemResponse;
import shop.example.shop.domain.cart.dto.CartUpdateRequest;
import shop.example.shop.domain.cart.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartItems(
            @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok(cartService.getCartItems(memberId));
    }

    // POST /api/cart/items - 장바구니에 상품 담기
    @PostMapping("/items")
    public ResponseEntity<Void> addCartItem(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CartAddRequest request
    ) {
        cartService.addItemToCart(memberId, request.getItemOptionId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // PATCH /api/cart/items/{itemOptionId} - 수량 변경
    @PatchMapping("/items/{itemOptionId}")
    public ResponseEntity<Void> updateItemQuantity(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long itemOptionId,
            @Valid @RequestBody CartUpdateRequest request
    ) {
        cartService.updateItemQuantity(memberId, itemOptionId, request.getQuantity());
        return ResponseEntity.ok().build();
    }

    // DELETE /api/cart/items/{itemOptionId} - 장바구니 상품 삭제
    @DeleteMapping("/items/{itemOptionId}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long itemOptionId
    ) {
        cartService.deleteItemFromCart(memberId, itemOptionId);
        return ResponseEntity.noContent().build();
    }
}
