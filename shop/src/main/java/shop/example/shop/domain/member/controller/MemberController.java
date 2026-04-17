package shop.example.shop.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.example.shop.domain.member.dto.MemberResponse;
import shop.example.shop.domain.member.service.MemberService;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // GET /api/members/me
    // @AuthenticationPrincipal: JwtFilter에서 저장한 인증 정보(회원 ID)를 꺼냄
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(
            @AuthenticationPrincipal Long memberId
    ) {
        MemberResponse response = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(response);
    }
}
