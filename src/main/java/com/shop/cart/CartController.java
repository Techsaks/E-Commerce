
package com.shop.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    public record AddCartItemRequest(
            @NotNull Long productId,
            @Positive int quantity
    ) {}

    public record UpdateCartItemRequest(
            @Positive int quantity
    ) {}

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request) {

        CartItem item = cartService.addItem(
                authentication.getName(),
                request.productId(),
                request.quantity()
        );

        return CartItemResponse.from(item);
    }

    @GetMapping("/items")
    public List<CartItemResponse> getItems(
            Authentication authentication) {

        return cartService.getItems(authentication.getName())
                .stream()
                .map(CartItemResponse::from)
                .toList();
    }

    @PatchMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateQuantity(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        cartService.updateQuantity(
                authentication.getName(),
                itemId,
                request.quantity()
        );
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            Authentication authentication,
            @PathVariable Long itemId) {

        cartService.removeItem(
                authentication.getName(), itemId);
    }
}
