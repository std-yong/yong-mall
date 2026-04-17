package shop.example.shop.domain.item.dto;

import lombok.Getter;
import shop.example.shop.domain.item.entity.Item;
import shop.example.shop.domain.item.entity.ItemOption;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ItemResponse {

    private final Long id;
    private final Long categoryId;
    private final String categoryName;
    private final String name;
    private final String description;
    private final int price;
    private final String status;
    private final List<ItemOptionResponse> options;
    private final LocalDateTime createdAt;

    // 일반 조회용: item의 options 리스트(DB에서 로딩된)를 사용
    public ItemResponse(Item item) {
        this(item, item.getOptions());
    }

    // 생성 직후 반환용: 저장한 옵션 목록을 직접 받아서 사용 (JPA 캐시 문제 우회)
    public ItemResponse(Item item, List<ItemOption> options) {
        this.id = item.getId();
        this.categoryId = item.getCategory().getId();
        this.categoryName = item.getCategory().getName();
        this.name = item.getName();
        this.description = item.getDescription();
        this.price = item.getPrice();
        this.status = item.getStatus().name();
        this.options = options.stream()
                .map(ItemOptionResponse::new)
                .collect(Collectors.toList());
        this.createdAt = item.getCreatedAt();
    }
}
