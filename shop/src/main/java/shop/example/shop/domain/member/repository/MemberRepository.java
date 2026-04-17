package shop.example.shop.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.example.shop.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원 조회 (로그인 시 사용)
    Optional<Member> findByEmail(String email);

    // 이메일 중복 체크 (회원가입 시 사용)
    boolean existsByEmail(String email);
}
