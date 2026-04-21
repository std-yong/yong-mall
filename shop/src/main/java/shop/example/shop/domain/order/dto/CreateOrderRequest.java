package shop.example.shop.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "배송지 주소를 입력해주세요.")
    private String deliveryAddress;
}