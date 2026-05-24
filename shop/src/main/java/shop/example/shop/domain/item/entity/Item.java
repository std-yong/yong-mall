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
