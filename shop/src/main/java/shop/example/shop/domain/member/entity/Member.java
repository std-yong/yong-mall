package shop.example.shop.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum Role {
        USER, ADMIN
    }

    @Builder
    public Member(String email, String password, String name,
                  String phone, String address, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.role = (role != null) ? role : Role.USER;
    }

    public void update(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
