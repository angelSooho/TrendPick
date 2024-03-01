package project.trendpick_pro.domain.answer.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AnswerRequest {
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}