package shop.example.shop.domain.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.item.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // 카테고리별 상품 목록 조회 (페이지네이션)
    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);

    // 상태별 상품 목록 조회 (페이지네이션)
    Page<Item> findByStatus(Item.Status status, Pageable pageable);

    // 카테고리 + 상태 조합 조회 (페이지네이션)
    Page<Item> findByCategoryIdAndStatus(Long categoryId, Item.Status status, Pageable pageable);

    // 상품명 키워드 검색 (대소문자 무시, LIKE %keyword%)
    Page<Item> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
