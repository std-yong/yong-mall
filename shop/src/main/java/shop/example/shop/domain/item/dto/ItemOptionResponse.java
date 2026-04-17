package shop.example.shop.domain.item.dto;

import lombok.Getter;
import shop.example.shop.domain.item.entity.ItemOption;

@Getter
public class ItemOptionResponse {

    private final Long id;
    private final String size;
    private final String color;
    private final int stockQuantity;

    public ItemOptionResponse(ItemOption option) {
        this.id = option.getId();
        this.size = option.getSize().name();
        this.color = option.getColor();
        this.stockQuantity = option.getStockQuantity();
    }
}
