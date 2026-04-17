package shop.example.shop.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// OncePerRequestFilter: 요청 1번당 딱 1번만 실행되는 필터
@RequiredArgsConstructor  // Lombok: final 필드를 받는 생성자 자동 생성
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더에서 토큰 꺼내기
        String token = resolveToken(request);

        // 2. 토큰이 있고 유효하면 인증 처리
        if (token != null && jwtUtil.validateToken(token)) {
            Long memberId = jwtUtil.getMemberId(token);
            String role = jwtUtil.getRole(token);

            // Spring Security에 인증 정보 등록
            // → 이후 Controller에서 @AuthenticationPrincipal 로 꺼낼 수 있음
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            memberId,           // principal (주체) - 여기선 회원 ID
                            null,               // credentials (비밀번호 - 이미 인증했으니 불필요)
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))  // 토큰의 role로 권한 설정
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 다음 필터로 넘기기 (필터 체인 계속 진행)
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 토큰 추출
    // 헤더 형식: "Bearer eyJhbGci..."
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);  // "Bearer " 7글자 제거하고 토큰만 반환
        }
        return null;
    }
}
