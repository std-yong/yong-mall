package shop.example.shop.domain.item.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.example.shop.domain.item.entity.Item;

import java.util.List;

@Getter
@NoArgsConstructor
public class ItemRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    private String description;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private int price;

    private Item.Status status;

    @Valid
    private List<ItemOptionRequest> options;
}
