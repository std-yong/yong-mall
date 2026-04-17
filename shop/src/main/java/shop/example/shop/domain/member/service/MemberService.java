package shop.example.shop.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.example.shop.domain.member.dto.LoginRequest;
import shop.example.shop.domain.member.dto.LoginResponse;
import shop.example.shop.domain.member.dto.MemberResponse;
import shop.example.shop.domain.member.dto.SignupRequest;
import shop.example.shop.domain.member.entity.Member;
import shop.example.shop.domain.member.repository.MemberRepository;
import shop.example.shop.global.exception.CustomException;
import shop.example.shop.global.exception.ErrorCode;
import shop.example.shop.global.jwt.JwtUtil;

import shop.example.shop.domain.cart.entity.Cart;
import shop.example.shop.domain.cart.repository.CartRepository;


@Service          // Spring이 이 클래스를 Service Bean으로 관리
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본적으로 읽기 전용 트랜잭션 (성능 최적화)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;  
    private final JwtUtil jwtUtil;

    // ================================
    // 회원가입
    // ================================
    @Transactional  // 쓰기 작업은 별도로 @Transactional (readOnly = false)
    public MemberResponse signup(SignupRequest request) {
        

        // 1. 이메일 중복 확인
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 비밀번호 암호화
        // "1234abcd!" → "$2a$10$abc123..." (BCrypt 해시)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 회원 Entity 생성 (Builder 패턴)
        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        

        // 4. DB 저장
        Member savedMember = memberRepository.save(member);
        cartRepository.save(new Cart(savedMember));  
        
        // 5. 저장된 회원 정보 반환 (DTO로 변환)
        return new MemberResponse(savedMember);
    }

    // ================================
    // 로그인
    // ================================
    public LoginResponse login(LoginRequest request) {

        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 비밀번호 확인
        // passwordEncoder.matches(): 입력한 비밀번호와 DB의 암호화된 비밀번호 비교
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. JWT 토큰 발급 (role 포함)
        String token = jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());

        return new LoginResponse(token);
    }

    // ================================
    // 내 정보 조회
    // ================================
    public MemberResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return new MemberResponse(member);
    }
}
