package project.trendpick_pro.domain.orders.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class OrderSellerResponse {

    private Page<OrderResponse> orderResponse;
    private int totalPricePerMonth;
}
