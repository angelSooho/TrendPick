package project.trendpick_pro.global.jmeter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.dto.MemberInfoResponse;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.global.kafka.KafkaProducerService;

@RestController
@RequiredArgsConstructor
@Profile("dev")
@RequestMapping("/jmeter")
public class JmeterController {

    private final OrderService orderService;
    private final ProductService productService;
    private final MemberService memberService;

    private final KafkaProducerService kafkaProducerService;

    @GetMapping("/member/login")
    public ResponseEntity<MemberInfoResponse> getMemberInfo(@MemberEmail String email) {
        Member member = memberService.findByEmail(email);
        MemberInfoResponse memberInfoResponse = MemberInfoResponse.of(member);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(memberInfoResponse, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @GetMapping("/order")
    @ResponseBody
    public void processOrder(@MemberEmail String email) {

        Long id = 3L;
        int quantity = 1;
        String size = "80";
        String color = "Sliver";

        orderService.productToOrder(email, id, quantity, size, color);
    }
}
