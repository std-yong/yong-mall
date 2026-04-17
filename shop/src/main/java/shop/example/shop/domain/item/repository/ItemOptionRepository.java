package shop.example.shop.domain.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.item.entity.ItemOption;

import java.util.List;

public interface ItemOptionRepository extends JpaRepository<ItemOption, Long> {

    // 상품의 옵션 목록 조회
    List<ItemOption> findByItemId(Long itemId);
}
