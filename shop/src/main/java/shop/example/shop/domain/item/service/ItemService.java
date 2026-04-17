package shop.example.shop.domain.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // ================================
    // 상품 목록 조회 (판매 중인 것만, 페이지네이션)
    // ================================
    public Page<ItemResponse> getItems(Long categoryId, Pageable pageable) {
        Page<Item> items;

        if (categoryId != null) {
            // 카테고리 + 판매 중 필터
            items = itemRepository.findByCategoryIdAndStatus(categoryId, Item.Status.SELLING, pageable);
        } else {
            // 판매 중 전체
            items = itemRepository.findByStatus(Item.Status.SELLING, pageable);
        }

        // Page<Item> → Page<ItemResponse>: map()으로 각 Item을 DTO로 변환
        return items.map(ItemResponse::new);
    }

    // ================================
    // 상품 키워드 검색
    // ================================
    public Page<ItemResponse> searchItems(String keyword, Pageable pageable) {
        return itemRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(ItemResponse::new);
    }

    // ================================
    // 상품 상세 조회
    // ================================
    public ItemResponse getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        return new ItemResponse(item);
    }

    // ================================
    // 상품 등록 (관리자)
    // ================================
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        // 1. 카테고리 조회
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. 상품 생성
        Item item = Item.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();

        Item savedItem = itemRepository.save(item);

        // 3. 옵션 생성 및 저장 (저장된 옵션을 리스트로 수집)
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

        // 4. 저장된 옵션 목록을 직접 넘겨서 응답 생성 (JPA 1차 캐시 우회)
        return new ItemResponse(savedItem, savedOptions);
    }

    // ================================
    // 상품 수정 (관리자)
    // ================================
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

        // update() 메서드는 Item 엔티티에 이미 정의되어 있음
        item.update(request.getName(), request.getDescription(), request.getPrice(), Item.Status.SELLING);

        return new ItemResponse(item);
    }

    // ================================
    // 상품 삭제 (관리자)
    // ================================
    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));
        itemRepository.delete(item);
    }
}
