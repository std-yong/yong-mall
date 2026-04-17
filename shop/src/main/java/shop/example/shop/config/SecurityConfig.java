package shop.example.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.example.shop.global.jwt.JwtAuthenticationFilter;
import shop.example.shop.global.jwt.JwtUtil;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // @PreAuthorize 어노테이션 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            // JWT 방식은 세션을 사용하지 않음
            // STATELESS: 서버가 로그인 상태를 기억하지 않음 → 매 요청마다 토큰으로 확인
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 허용
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // 상품/카테고리 조회는 비로그인도 가능
                // "/api/items" (목록)과 "/api/items/**" (상세/검색) 둘 다 허용
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/items", "/api/items/**", "/api/categories", "/api/categories/**").permitAll()
                // 관리자만 접근 가능
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 나머지는 로그인 필요
                .anyRequest().authenticated()
            )

            // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 등록
            // → 모든 요청이 들어올 때 JWT 필터가 먼저 실행됨
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // 비밀번호 암호화 방식: BCrypt
    // BCrypt는 단방향 해시 → 복호화 불가능, 매번 다른 해시값 생성
    // 회원가입 시 암호화, 로그인 시 비교에 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
