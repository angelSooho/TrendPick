package project.trendpick_pro.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    /* 400 */
    BAD_REQUEST(40001, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    /* 401 */
    UNAUTHORIZED(40101, HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다."),
    INVALID_TOKEN(40102, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    /* 404 */
    NOT_FOUND(40400, HttpStatus.NOT_FOUND, "페이지를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "멤버가 존재하지 않습니다."),
    PRODUCT_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "상품이 존재 하지 않습니다."),
    ORDER_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    ORDERITEM_OUT(40404, HttpStatus.NOT_FOUND, "재고가 없습니다."),
    ORDERITEM_NOT_FOUND(40405, HttpStatus.NOT_FOUND, "주문 상품이 존재하지 않습니다."),
    DOCUMENT_NOT_FOUND(40406, HttpStatus.NOT_FOUND, "검색 객체가 존재하지 않습니다."),
    ASK_NOT_FOUND(40407, HttpStatus.NOT_FOUND, "문의가 존재하지 않습니다."),
    COUPON_NOT_FOUND(40408, HttpStatus.NOT_FOUND, "쿠폰이 존재하지 않습니다."),
    NOTIFICATION_NOT_FOUND(40409, HttpStatus.NOT_FOUND, "알림이 존재하지 않습니다."),
    NOTIFICATIONTYPE_NOT_FOUND(40410, HttpStatus.NOT_FOUND, "알림 타입이 존재하지 않습니다."),
    NOT_MATCH(40411, HttpStatus.NOT_FOUND, "일치하는 정보가 없습니다."),

    /* 403 */
    FORBIDDEN(40301, HttpStatus.FORBIDDEN, "권한이 없습니다."),
    ASK_NOT_MATCH(40302, HttpStatus.FORBIDDEN, "문의 권한이 다를 경우"),
    MEMBER_NOT_MATCH(40303, HttpStatus.FORBIDDEN, "멤버 권한이 다를 경우"),

    /* 423 */
    LOCK_ALREADY_USED(42301, HttpStatus.LOCKED, "이미 사용중인 락인 경우"),;

    private final int code;
    private final HttpStatus status;
    private final String description;

    ErrorCode(int code, HttpStatus status, String description) {
        this.status = status;
        this.code = code;
        this.description = description;
    }
}
