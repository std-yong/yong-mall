package shop.example.shop.domain.item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Category와 다대일(N:1) 관계
    // item 여러 개가 하나의 category에 속함
    // @JoinColumn: 실제 FK 컬럼명 지정 (item 테이블의 category_id 컬럼)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    // Item과 ItemOption은 일대다(1:N) 관계
    // mappedBy = "item" → ItemOption 클래스의 item 필드가 연관관계의 주인
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<ItemOption> options = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum Status {
        SELLING, SOLD_OUT, STOP
    }

    @Builder
    public Item(Category category, String name, String description, int price) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = Status.SELLING;
    }

    public void update(String name, String description, int price, Status status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }
}
