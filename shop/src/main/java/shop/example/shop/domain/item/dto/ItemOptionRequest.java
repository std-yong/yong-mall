package shop.example.shop.domain.item.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.example.shop.domain.item.entity.ItemOption;

@Getter
@NoArgsConstructor
public class ItemOptionRequest {

    @NotNull(message = "사이즈는 필수입니다.")
    private ItemOption.Size size;

    @NotBlank(message = "색상은 필수입니다.")
    private String color;

    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private int stockQuantity;
}
