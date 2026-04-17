package shop.example.shop.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component  // Spring이 이 클래스를 Bean으로 관리 → 다른 클래스에서 @Autowired 또는 생성자 주입으로 사용 가능
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;

    // application.properties의 값을 @Value로 주입받음
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        // 문자열 비밀키 → JWT 서명용 SecretKey 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    // ================================
    // 토큰 생성
    // ================================
    public String generateToken(Long memberId, String email, String role) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))  // 토큰의 주인 (회원 ID)
                .claim("email", email)              // 추가 데이터
                .claim("role", role)                // 권한 정보 추가
                .issuedAt(new Date())               // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 만료 시간
                .signWith(secretKey)                // 비밀키로 서명
                .compact();                         // 문자열로 변환
    }

    // ================================
    // 토큰에서 회원 ID 추출
    // ================================
    public Long getMemberId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // ================================
    // 토큰에서 이메일 추출
    // ================================
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // ================================
    // 토큰에서 권한(role) 추출
    // ================================
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ================================
    // 토큰 유효성 검증
    // ================================
    public boolean validateToken(String token) {
        try {
            getClaims(token);  // 파싱이 성공하면 유효한 토큰
            return true;
        } catch (Exception e) {
            return false;  // 만료, 위조, 형식 오류 등 모두 false
        }
    }

    // ================================
    // 토큰 파싱 (내부 메서드)
    // ================================
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)   // 비밀키로 서명 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();           // 페이로드(데이터) 반환
    }
}
