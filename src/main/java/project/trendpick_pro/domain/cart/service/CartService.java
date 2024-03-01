package project.trendpick_pro.domain.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.cart.entity.Cart;
import project.trendpick_pro.domain.cart.entity.CartItem;
import project.trendpick_pro.domain.cart.entity.dto.request.CartItemRequest;
import project.trendpick_pro.domain.cart.repository.CartItemRepository;
import project.trendpick_pro.domain.cart.repository.CartRepository;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.domain.orders.entity.Order;
import project.trendpick_pro.domain.orders.entity.dto.request.CartToOrderRequest;
import project.trendpick_pro.domain.product.entity.product.Product;
import project.trendpick_pro.domain.product.service.ProductService;
import project.trendpick_pro.domain.tags.favoritetag.service.FavoriteTagService;
import project.trendpick_pro.domain.tags.tag.entity.TagType;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final ProductService productService;
    private final MemberService memberService;
    private final FavoriteTagService favoriteTagService;

    @Transactional
    public CartItem addCartItem(String email, CartItemRequest cartItemRequest) {
        Member member = memberService.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());
        Product product = productService.findById(cartItemRequest.getProductId());

        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (cartItem == null)
            cartItem = CartItem.of(cart, product, cartItemRequest);

        cartItem.updateCount(cartItemRequest.getQuantity());
        cartItemRepository.save(cartItem);

        favoriteTagService.updateTag(member, product, TagType.CART);
        return cartItem;
    }

    @Transactional
    public void updateCartItemCount (Long cartItemId,int quantity){
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new BaseException(ErrorCode.BAD_REQUEST, "해당 상품이 존재하지 않습니다."));
        cartItem.updateCount(quantity);
    }

    @Transactional
    public void deleteCartItem (Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElse(null);
        if (cartItem != null) {
            Cart cart = cartItem.getCart();
            cart.updateTotalCount(-1 * cartItem.getQuantity());
        }
        cartItemRepository.deleteById(cartItemId);
    }

    public List<CartItem> getAllCartItems(Cart cart){
        return cartItemRepository.findAllByCart(cart);
    }

    public List<CartItem> getSelectedCartItems(String email, CartToOrderRequest request){
        Cart cart = getCartByUser(email);
        List<CartItem> cartItemList = new ArrayList<>();

        for (Long productId : request.getSelectedItems()) {
            CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
            if(cartItem!=null)
                cartItemList.add(cartItem);
        }
        return cartItemList;
    }

    public Cart getCartByUser (String email){
        Member member = memberService.findByEmail(email);
        return cartRepository.findByMemberId(member.getId());
    }

    private static List<CartItem> matchedCartProducts(List<CartItem> cartProducts, List<String> productCodes) {
        return cartProducts.stream()
                .filter(cartItem -> productCodes.contains(cartItem.getProduct().getProductCode()))
                .collect(Collectors.toList());
    }

    private static List<String> collectProductCode(Order order) {
        return order.getOrderItems().stream()
                .map(orderItem -> orderItem.getProduct().getProductCode()).collect(Collectors.toList());
    }
}
