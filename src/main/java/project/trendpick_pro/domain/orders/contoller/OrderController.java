package project.trendpick_pro.domain.orders.contoller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.orders.entity.dto.request.CartToOrderRequest;
import project.trendpick_pro.domain.orders.entity.dto.request.ProductOrderRequest;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderDetailResponse;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderResponse;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderSellerResponse;
import project.trendpick_pro.domain.orders.service.OrderService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @GetMapping("{orderId}/form")
    public String showOrderForm(@PathVariable("orderId") Long orderId, Model model){
        model.addAttribute("order", orderService.findById(orderId));
        return "trendpick/orders/order-form";
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @PostMapping("/order/cart")
    public ResponseEntity<Void> cartToOrder(
            @MemberEmail String email,
            @RequestBody CartToOrderRequest request) {
        orderService.cartToOrder(email, request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @PostMapping("/order/product")
    @ResponseBody
    public ResponseEntity<Void> productToOrder(@MemberEmail String email, @RequestBody ProductOrderRequest request) {
        orderService.productToOrder(email, request.getProductId(), request.getQuantity(), request.getSize(), request.getColor());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<Void> cancel(@PathVariable("orderId") Long orderId) {
        orderService.cancel(orderId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<OrderSellerResponse> getOrders(@MemberEmail String email, @RequestParam(value = "page", defaultValue = "0") int offset) {
        return ResponseEntity.ok().body(orderService.getOrders(email, offset));
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> showOrder(@MemberEmail String email,
            @PathVariable("orderId") Long orderId){
        return ResponseEntity.ok().body(orderService.getOrderItems(email, orderId));
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @GetMapping("/usr/refunds")
    public ResponseEntity<Page<OrderResponse>> showCanceledOrderListByMember(@MemberEmail String email, @RequestParam(value = "page", defaultValue = "0") int offset){
        return ResponseEntity.ok().body(orderService.getCanceledOrders(email, offset));
    }
}
