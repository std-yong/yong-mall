package shop.example.shop.domain.member.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import shop.example.shop.domain.cart.entity.Cart;
import shop.example.shop.domain.cart.repository.CartRepository;
import shop.example.shop.domain.member.dto.LoginRequest;
import shop.example.shop.domain.member.dto.LoginResponse;
import shop.example.shop.domain.member.dto.MemberResponse;
import shop.example.shop.domain.member.dto.SignupRequest;
import shop.example.shop.domain.member.entity.Member;
import shop.example.shop.domain.member.repository.MemberRepository;
import shop.example.shop.global.exception.CustomException;
import shop.example.shop.global.exception.ErrorCode;
import shop.example.shop.global.jwt.JwtUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private MemberService memberService;

    @Test
    void signup_이메일_중복이면_예외_발생() {
        // given
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "test@test.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "name", "테스트유저");

        when(memberRepository.existsByEmail("test@test.com")).thenReturn(true);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> memberService.signup(request));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    void signup_성공시_회원과_장바구니_함께_저장() {
        // given
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", "test@test.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "name", "테스트유저");

        Member savedMember = Member.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .name("테스트유저")
                .build();

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart(savedMember));

        // when
        MemberResponse response = memberService.signup(request);

        // then
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        verify(memberRepository).save(any(Member.class));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void login_비밀번호_틀리면_예외_발생() {
        // given
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "test@test.com");
        ReflectionTestUtils.setField(request, "password", "wrongPassword");

        Member member = Member.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .name("테스트유저")
                .build();

        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> memberService.login(request));

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    void login_성공시_토큰_반환() {
        // given
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", "test@test.com");
        ReflectionTestUtils.setField(request, "password", "password123");

        Member member = Member.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .name("테스트유저")
                .build();

        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any(), anyString(), anyString())).thenReturn("mocked.jwt.token");

        // when
        LoginResponse response = memberService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
    }
}
