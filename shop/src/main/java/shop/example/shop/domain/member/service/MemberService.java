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


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        Member savedMember = memberRepository.save(member);
        cartRepository.save(new Cart(savedMember));

        return new MemberResponse(savedMember);
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtUtil.generateToken(member.getId(), member.getEmail(), member.getRole().name());
        return new LoginResponse(token);
    }

    public MemberResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        return new MemberResponse(member);
    }
}
