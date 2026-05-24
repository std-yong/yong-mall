package shop.example.shop.domain.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import shop.example.shop.domain.item.dto.ItemOptionRequest;
import shop.example.shop.domain.item.dto.ItemRequest;
import shop.example.shop.domain.item.dto.ItemResponse;
import shop.example.shop.domain.item.entity.Category;
import shop.example.shop.domain.item.entity.Item;
import shop.example.shop.domain.item.entity.ItemOption;
import shop.example.shop.domain.item.repository.CategoryRepository;
import shop.example.shop.domain.item.repository.ItemOptionRepository;
import shop.example.shop.domain.item.repository.ItemRepository;
import shop.example.shop.global.exception.CustomException;
import shop.example.shop.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemOptionRepository itemOptionRepository;
    private final CategoryRepository categoryRepository;

    public Page<ItemResponse> getItems(Long categoryId, Pageable pageable) {
        Page<Item> items = (categoryId != null)
                ? itemRepository.findByCategoryIdAndStatus(categoryId, Item.Status.SELLING, pageable)
                : itemRepository.findByStatus(Item.Status.SELLING, pageable);
        return items.map(ItemResponse::new);
    }

    public Page<ItemResponse> searchItems(String keyword, Pageable pageable) {
        return itemRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(ItemResponse::new);
    }

    public ItemResponse getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        return new ItemResponse(item);
    }

    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        Item item = Item.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        Item savedItem = itemRepository.save(item);

        // JPA 1차 캐시를 우회하기 위해 저장된 옵션을 직접 수집해서 반환
        List<ItemOption> savedOptions = new ArrayList<>();
        if (request.getOptions() != null) {
            for (ItemOptionRequest optionRequest : request.getOptions()) {
                ItemOption option = ItemOption.builder()
                        .item(savedItem)
                        .size(optionRequest.getSize())
                        .color(optionRequest.getColor())
                        .stockQuantity(optionRequest.getStockQuantity())
                        .build();
                savedOptions.add(itemOptionRepository.save(option));
            }
        }

        return new ItemResponse(savedItem, savedOptions);
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        Item.Status status = (request.getStatus() != null) ? request.getStatus() : item.getStatus();
        item.update(request.getName(), request.getDescription(), request.getPrice(), status);

        return new ItemResponse(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        itemRepository.delete(item);
    }
}
