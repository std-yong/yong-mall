package shop.example.shop.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartAddRequest {

    @NotNull(message = "상품 옵션은 필수입니다.")
    private Long itemOptionId;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private int quantity;
}
