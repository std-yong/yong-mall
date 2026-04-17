package shop.example.shop.domain.member.dto;

import lombok.Getter;
import shop.example.shop.domain.member.entity.Member;

import java.time.LocalDateTime;

@Getter
public class MemberResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String role;
    private LocalDateTime createdAt;

    // Entity → DTO 변환
    // 비밀번호 같은 민감한 정보는 포함하지 않음
    public MemberResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.phone = member.getPhone();
        this.address = member.getAddress();
        this.role = member.getRole().name();
        this.createdAt = member.getCreatedAt();
    }
}
