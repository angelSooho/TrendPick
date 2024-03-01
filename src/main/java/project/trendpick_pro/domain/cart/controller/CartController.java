package project.trendpick_pro.domain.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.cart.entity.Cart;
import project.trendpick_pro.domain.cart.entity.CartItem;
import project.trendpick_pro.domain.cart.entity.dto.request.CartItemRequest;
import project.trendpick_pro.domain.cart.service.CartService;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/cart")
public class CartController {

    private final CartService cartService;

    @PreAuthorize("hasAuthority('MEMBER')")
    @GetMapping("/list")
    public ResponseEntity<List<CartItem>> showCart(@MemberEmail String email) {
        Cart cart = cartService.getCartByUser(email);
        return ResponseEntity.ok().body(cartService.getAllCartItems(cart));
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @PostMapping("/add")
    public ResponseEntity<CartItem> addItem(
            @MemberEmail String email,
            @Valid @RequestBody CartItemRequest cartItemRequest) {
        return ResponseEntity.ok().body(cartService.addCartItem(email, cartItemRequest));
    }

    @PreAuthorize("hasAuthority({'MEMBER'})")
    @GetMapping("delete/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable("cartItemId") Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok().build();
    }

    // 장바구니에서 수량 변경
    @PreAuthorize("hasAuthority({'MEMBER'})")
    @PostMapping("/update")
    public ResponseEntity<Void> updateCount(@RequestParam("cartItemId") Long cartItemId,
                              @RequestParam("quantity") int newQuantity) {
        cartService.updateCartItemCount(cartItemId, newQuantity);
        return ResponseEntity.ok().build();
    }
}
