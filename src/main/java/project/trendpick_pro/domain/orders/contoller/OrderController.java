package project.trendpick_pro.domain.orders.contoller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import project.trendpick_pro.domain.common.base.rq.Rq;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.dto.MemberInfoDto;
import project.trendpick_pro.domain.member.exception.MemberNotFoundException;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.orders.entity.OrderStatus;
import project.trendpick_pro.domain.orders.entity.dto.request.OrderForm;
import project.trendpick_pro.domain.orders.entity.dto.request.OrderSearchCond;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderItemDto;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderResponse;
import project.trendpick_pro.domain.orders.service.OrderService;
import project.trendpick_pro.domain.product.entity.Product;
import project.trendpick_pro.domain.product.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("trendpick/orders")
public class OrderController {

    private final OrderService orderService;
    private final Rq rq;
    private final MemberService memberService;
    private final ProductRepository productRepository;

    @GetMapping("/order")
    public String orderForm(@RequestParam("orderForm") OrderForm orderForm,
                            HttpServletRequest req,
                            Model model) {
//        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);
//        if (flashMap != null) {
//            orderForm = (OrderForm) flashMap.get("orderForm");
//            model.addAttribute("orderForm", orderForm);
//        }
        model.addAttribute("orderForm", orderForm);
        return "trendpick/orders/order";
    }

    @PostMapping("/order")
    public synchronized String processOrder(@ModelAttribute("orderForm") OrderForm orderForm) {
        Member member = rq.CheckMember().get();
        log.info(member.getUsername());
        log.info(orderForm.getMemberInfo().getName());
        log.info(orderForm.getMemberInfo().getAddress());
        log.info(orderForm.getMemberInfo().getEmail());
        log.info(orderForm.getPaymentMethod());
        log.info(orderForm.getOrderItems().get(0).getProductName());
        if (member.getId() != orderForm.getMemberInfo().getMemberId())
            throw new RuntimeException("잘못된 접근입니다.");

        orderService.order(member, orderForm);
        return "redirect:/trendpick/orders/list";
    }

    @PostMapping("/cart")
    public String cartToOrder(@RequestParam("selectedItems") List<Long> selectedItems, Model model) {
        model.addAttribute("orderForm"
                , orderService.cartToOrder(rq.CheckMember().get(), selectedItems));

        return "trendpick/orders/order";
    }

    @GetMapping("/order/product")
    public String orderProduct(@RequestParam("product") Long productId,
                               @RequestParam("count") int count, Model model) {
        model.addAttribute("orderForm", orderService.productToOrder(rq.CheckMember().get(), productId, count));
        return "trendpick/orders/order";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public String orderListByMember(
            @RequestParam(value = "page", defaultValue = "0") int offset,
            Model model) {

        model.addAttribute("orderList", orderService.findAllByMember(rq.CheckMember().get(), offset));
        return "trendpick/usr/member/orders";
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancel(orderId);
        return "redirect:trendpick/usr/member/orders";
    }
}
