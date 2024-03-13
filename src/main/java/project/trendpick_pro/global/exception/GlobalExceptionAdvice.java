package project.trendpick_pro.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionAdvice {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, BaseException e) {
        log.error("[exceptionHandle] ex", e);
        ErrorResponse response = new ErrorResponse(e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException() {
        return ResponseEntity.status(400).body(new ErrorResponse(400, "파일 크기는 " + maxFileSize + " 이하여야 합니다."));
    }
}
