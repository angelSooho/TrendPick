package project.trendpick_pro.global.crypto.jwt.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.trendpick_pro.global.exception.ErrorCode;

import java.time.LocalDateTime;

@Getter
public class FilterExceptionResponse {

    private final LocalDateTime timestamp;
    private final String error;
    private final String code;
    private final String message;
    private final String path;

    private FilterExceptionResponse(LocalDateTime timestamp, String name, int code, HttpStatus status, String message, String path) {
        this.timestamp = timestamp;
        this.error = name;
        this.code = String.valueOf(code);
        this.message = message;
        this.path = path;
    }

    public static FilterExceptionResponse of(FilterException e, String path) {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorCode errorCode = e.getErrorCode();
        return new FilterExceptionResponse(
                timestamp,
                errorCode.name(),
                errorCode.getCode(),
                errorCode.getStatus(),
                e.getMessage(),
                path);
    }
}
