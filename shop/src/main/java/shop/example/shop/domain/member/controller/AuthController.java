package shop.example.shop.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.example.shop.domain.member.dto.LoginRequest;
import shop.example.shop.domain.member.dto.LoginResponse;
import shop.example.shop.domain.member.dto.MemberResponse;
import shop.example.shop.domain.member.dto.SignupRequest;
import shop.example.shop.domain.member.service.MemberService;

@RestController   // @Controller + @ResponseBody. 모든 메서드가 JSON 응답을 반환함
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    // POST /api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(
            @Valid @RequestBody SignupRequest request  // @Valid: DTO의 유효성 검증 실행
    ) {
        MemberResponse response = memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);  // 201 Created
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(response);  // 200 OK
    }
}
