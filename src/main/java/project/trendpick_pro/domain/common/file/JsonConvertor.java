package project.trendpick_pro.domain.common.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class JsonConvertor {

    private final ObjectMapper objectMapper;

    public <T> T readValue(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "json 변환에 실패했습니다.");
        }
    }
}
