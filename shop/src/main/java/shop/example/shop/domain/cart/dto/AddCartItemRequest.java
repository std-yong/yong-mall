package shop.example.shop.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
    @NotNull Long itemOptionId,
    @Min(1) int quantity
) {}

