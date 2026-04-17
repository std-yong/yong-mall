package shop.example.shop.global.exception;

import lombok.Getter;

// RuntimeException을 상속: 체크 예외(checked)가 아닌 언체크 예외(unchecked)로 만들어서
// throws 선언 없이 어디서든 자유롭게 던질 수 있음
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
