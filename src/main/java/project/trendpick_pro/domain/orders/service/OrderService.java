package project.trendpick_pro.domain.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.cart.entity.CartItem;
import project.trendpick_pro.domain.cart.service.CartService;
import project.trendpick_pro.domain.delivery.entity.Delivery;
import project.trendpick_pro.domain.delivery.entity.DeliveryState;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRole;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.orders.entity.Order;
import project.trendpick_pro.domain.orders.entity.OrderItem;
import project.trendpick_pro.domain.orders.entity.OrderStatus;
import project.trendpick_pro.domain.orders.entity.dto.request.CartToOrderRequest;
import project.trendpick_pro.domain.orders.entity.dto.request.OrderSearchCond;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderDetailResponse;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderResponse;
import project.trendpick_pro.domain.orders.entity.dto.response.OrderSellerResponse;
import project.trendpick_pro.domain.orders.repository.OrderItemRepository;
import project.trendpick_pro.domain.orders.repository.OrderRepository;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.tags.favoritetag.service.FavoriteTagService;
import project.trendpick_pro.domain.tags.tag.entity.TagType;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;
import project.trendpick_pro.global.kafka.KafkaProducerService;
import project.trendpick_pro.global.kafka.kafkasave.entity.OutboxMessage;
import project.trendpick_pro.global.kafka.kafkasave.service.OutboxMessageService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    private final CartService cartService;
    private final ProductService productService;
    private final FavoriteTagService favoriteTagService;

    private final MemberService memberService;
    private final OutboxMessageService outboxMessageService;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public void cartToOrder(String email, CartToOrderRequest request) {
        Member member = memberService.findByEmail(email);
        if(member.getAddress().trim().isEmpty()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "주소를 입력해주세요.");
        }
        if(request.getSelectedItems().isEmpty()){
            throw new BaseException(ErrorCode.BAD_REQUEST, "상품을 선택한 후 주문해주세요.");
        }
        List<CartItem> cartItems = cartService.getSelectedCartItems(email, request);
        if (!Objects.equals(cartItems.get(0).getCart().getMember().getId(), member.getId())) {
            throw new BaseException(ErrorCode.NOT_MATCH, "다른 사용자의 장바구니에는 접근할 수 없습니다.");
        }
        Order order = createOrder(member, cartItems);

        OutboxMessage message = createOutboxMessage(order);
        kafkaProducerService.sendMessage(message.getId());
    }

    @Transactional
    public void productToOrder(String email, Long productId, int quantity, String size, String color) {
        Member member = memberService.findByEmail(email);
        try {
            Order saveOrder = orderRepository.save(
                    Order.createOrder(member, new Delivery(member.getAddress()),
                            List.of(OrderItem.of(productService.findById(productId), quantity, size, color))
                    )
            );
            log.info("재고 : {}", saveOrder.getOrderItems().get(0).getProduct().getProductOption().getStock());
            OutboxMessage message = createOutboxMessage(saveOrder);
            kafkaProducerService.sendMessage(message.getId());
        } catch (BaseException e) {
            throw new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 상품입니다.");
        }
    }

    @Transactional
    public void tryOrder(String id) throws JsonProcessingException {
        Order order = orderRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 주문입니다."));
        String email = order.getMember().getEmail();
        try {
            order.updateStatus(OrderStatus.ORDERED);
            sendMessage(order.getId(), "Success", email);
        } catch (BaseException e) {
            if (order.getOrderStatus() == OrderStatus.TEMP) {
                order.cancelTemp();
            }
            log.error("주문 실패 : {}", e.getMessage());
            sendMessage(order.getId(), "Fail", email);
        }
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 주문입니다."));
        if(order.getOrderStatus() == OrderStatus.CANCELED) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 취소된 주문입니다.");
        } else if (order.getDelivery().getState() == DeliveryState.COMPLETED) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "이미 배송이 완료된 주문입니다.");
        }
        if (order.getDelivery().getState() == DeliveryState.DELIVERY_ING) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "배송 중인 주문은 취소할 수 없습니다.");
        }
        order.cancel();
    }

    @Transactional
    public void delete(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 주문입니다."));
        orderRepository.delete(order);
    }

    public OrderDetailResponse getOrderItems(String email, Long orderId) {
        Member member = memberService.findByEmail(email);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 주문입니다."));
        if (order.getOrderStatus() == OrderStatus.TEMP) {
            orderRepository.delete(order);
            throw new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 주문입니다.");
        }
        if (!Objects.equals(order.getMember().getId(), member.getId())) {
            throw new BaseException(ErrorCode.NOT_MATCH, "본인의 주문이 아닙니다.");
        }
        return OrderDetailResponse.of(order, orderRepository.findOrderItemsByOrderId(orderId));
    }

    public OrderSellerResponse getOrders(String email, int offset) {
        Member member = memberService.findByEmail(email);
        Page<OrderResponse> findOrders = settingOrderByMemberStatus(member, offset);
        List<OrderResponse> validOrders = checkingTempStatus(findOrders);

        Page<OrderResponse> response = new PageImpl<>(validOrders, findOrders.getPageable(), validOrders.size());
        return new OrderSellerResponse(response, settlementOfSales(member, LocalDate.now()));
    }

    public int settlementOfSales(Member member, LocalDate registeredDate) {
        return orderRepository.findAllByMonth(new OrderSearchCond(member.getBrand()), registeredDate);
    }

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 주문입니다."));
    }

    public Page<OrderResponse> getCanceledOrders(String email, int offset) {
        Member member = memberService.findByEmail(email);
        OrderSearchCond cond = new OrderSearchCond(member.getId(), OrderStatus.CANCELED);
        return orderRepository.findAllByMember(cond, PageRequest.of(offset, 10));
    }

    public List<OrderItem> getOrdersByCreatedDateBetweenByIdAsc(LocalDateTime fromDate, LocalDateTime toDate) {
        return orderItemRepository.findAllByCreatedDateBetween(fromDate, toDate).orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "존재하지 않는 주문입니다."));
    }

    private Order createOrder(Member member, List<CartItem> cartItems) {
        List<OrderItem> orderItems = createOrderItem(member, cartItems);
        Order order = Order.createOrder(member, new Delivery(member.getAddress()), orderItems);
        return orderRepository.save(order);
    }

    private List<OrderItem> createOrderItem(Member member, List<CartItem> cartItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = checkingProductStock(cartItem);
            favoriteTagService.updateTag(member, product, TagType.ORDER);
            orderItems.add(OrderItem.of(product, cartItem));
        }
        return orderItems;
    }

    private Product checkingProductStock(CartItem cartItem) {
        Product product = productService.findById(cartItem.getProduct().getId());
        if (product.getProductOption().getStock() < cartItem.getQuantity()) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "재고가 부족합니다.");
        }
        return product;
    }

    private OutboxMessage createOutboxMessage(Order order) {
        OutboxMessage message = new OutboxMessage("orders",
                String.valueOf(order.getId()), String.valueOf(order.getId()));
        outboxMessageService.save(message);
        return message;
    }

    private void sendMessage(Long orderId, String message, String email) throws JsonProcessingException {
        kafkaProducerService.sendMessage(orderId, message, email);
    }

    private Page<OrderResponse> settingOrderByMemberStatus(Member member, int offset) {
        Page<OrderResponse> findOrders;
        PageRequest pageable = PageRequest.of(offset, 10);
        if (member.getRole().equals(MemberRole.MEMBER)) {
            OrderSearchCond cond = new OrderSearchCond(member.getId());
            findOrders = orderRepository.findAllByMember(cond, pageable);
        } else {
            OrderSearchCond cond = new OrderSearchCond(member.getBrand());
            findOrders = orderRepository.findAllBySeller(cond, pageable);
        }
        return findOrders;
    }

    private static List<OrderResponse> checkingTempStatus(Page<OrderResponse> findOrders) {
        return findOrders.stream()
                .filter(orderResponse -> !Objects.equals(orderResponse.getOrderStatus(), OrderStatus.TEMP.toString()))
                .collect(Collectors.toList());
    }
}
