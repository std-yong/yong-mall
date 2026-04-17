package shop.example.shop.domain.item.dto;

import lombok.Getter;
import shop.example.shop.domain.item.entity.Category;

@Getter
public class CategoryResponse {

    private final Long id;
    private final String name;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}
