package shop.example.shop.domain.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.item.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
