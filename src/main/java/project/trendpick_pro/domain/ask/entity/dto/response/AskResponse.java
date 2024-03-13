package project.trendpick_pro.domain.ask.entity.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import org.springframework.data.domain.Page;
import project.trendpick_pro.domain.ask.entity.Ask;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AskResponse {

    private String productName;
    private String memberName;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdDate;

    @Builder
    @QueryProjection
    public AskResponse(String productName, String memberName, String title,
                       String content, String status, LocalDateTime createdDate) {
        this.productName = productName;
        this.memberName = memberName;
        this.title = title;
        this.content = content;
        this.status = status;
        this.createdDate = createdDate;
    }

    public static AskResponse of (Ask ask) {
        return AskResponse.builder()
                .memberName(ask.getAuthor().getNickName())
                .productName(ask.getProduct().getTitle())
                .title(ask.getTitle())
                .content(ask.getContent())
                .status(ask.getStatus().getValue())
                .createdDate(ask.getCreatedDate())
                .build();
    }

    public static Page<AskResponse> of(Page<Ask> asks){
        return asks.map(AskResponse::of);
    }
}
