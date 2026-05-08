package shop.example.shop.global.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final String code;
    private final String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

    public ErrorResponse(ErrorCode errorCode, String message) {
        this.code = errorCode.name();
        this.message = message;
    }
}
