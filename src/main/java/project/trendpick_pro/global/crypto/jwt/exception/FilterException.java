package project.trendpick_pro.global.crypto.jwt.exception;

import lombok.Getter;
import project.trendpick_pro.global.exception.ErrorCode;

@Getter
public class FilterException extends RuntimeException {
    private final ErrorCode errorCode;

    public FilterException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
