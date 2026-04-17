package shop.example.shop.domain.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.example.shop.domain.item.dto.CategoryRequest;
import shop.example.shop.domain.item.dto.CategoryResponse;
import shop.example.shop.domain.item.entity.Category;
import shop.example.shop.domain.item.repository.CategoryRepository;
import shop.example.shop.global.exception.CustomException;
import shop.example.shop.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리 전체 조회
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    // 카테고리 생성 (관리자)
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = new Category(request.getName());
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved);
    }

    // 카테고리 삭제 (관리자)
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }
}
