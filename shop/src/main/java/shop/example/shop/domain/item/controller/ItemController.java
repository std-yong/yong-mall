package shop.example.shop.domain.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import shop.example.shop.domain.item.dto.ItemRequest;
import shop.example.shop.domain.item.dto.ItemResponse;
import shop.example.shop.domain.item.service.ItemService;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // GET /api/items?categoryId=1&page=0&size=20
    // @PageableDefault: Pageable의 기본값 설정 (size=20, 최신순 정렬)
    // @RequestParam(required = false): 없어도 되는 선택적 파라미터
    @GetMapping
    public ResponseEntity<Page<ItemResponse>> getItems(
            @RequestParam(required = false) Long categoryId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(itemService.getItems(categoryId, pageable));
    }

    // GET /api/items/search?keyword=청바지&page=0&size=20
    @GetMapping("/search")
    public ResponseEntity<Page<ItemResponse>> searchItems(
            @RequestParam String keyword,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(itemService.searchItems(keyword, pageable));
    }

    // GET /api/items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItem(id));
    }

    // POST /api/items (관리자만)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(request));
    }

    // PUT /api/items/{id} (관리자만)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request
    ) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    // DELETE /api/items/{id} (관리자만)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
