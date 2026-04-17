package shop.example.shop.global.exception;

import lombok.Getter;

// 에러 응답 DTO: 클라이언트에게 전달할 에러 정보 구조
@Getter
public class ErrorResponse {

    private final String code;     // 에러 코드명 (예: "MEMBER_NOT_FOUND")
    private final String message;  // 사람이 읽을 수 있는 메시지

    public ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }
}
