package shop.example.shop.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity                          // 이 클래스가 DB 테이블과 연결된다는 표시
@Table(name = "member")          // 연결할 테이블 이름 지정
@Getter                          // Lombok: 모든 필드의 getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA가 내부적으로 사용하는 기본 생성자 (외부에서 직접 new 못하게 막음)
public class Member {

    @Id                                                    // 기본키(PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // AUTO_INCREMENT
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

    @Enumerated(EnumType.STRING)           // Enum을 DB에 문자열로 저장 ("USER", "ADMIN")
    @Column(nullable = false, length = 10)
    private Role role;

    @CreationTimestamp                     // INSERT 시 자동으로 현재 시간 저장
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp                       // UPDATE 시 자동으로 현재 시간 갱신
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 회원 역할 (USER 또는 ADMIN)
    public enum Role {
        USER, ADMIN
    }

    // Builder 패턴으로 객체 생성
    // ex) Member.builder().email("a@b.com").name("홍길동").build()
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

    // 회원 정보 수정 메서드
    public void update(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // 비밀번호 변경 메서드
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
