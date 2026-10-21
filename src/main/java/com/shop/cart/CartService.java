
package com.shop.cart;

import com.shop.product.Product;
import com.shop.product.ProductRepository;
import com.shop.user.User;
import com.shop.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser_Id(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional
    public CartItem addItem(String email, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }

        User user = getUser(email);
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Product is inactive");
        }

        if (quantity > product.getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Insufficient stock");
        }

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(-1L, cart.getId())
                .orElse(null);

        // Find an existing item for this product in this cart.
        List<CartItem> items =
                cartItemRepository.findAllByCart_Id(cart.getId());

        for (CartItem existing : items) {
            if (existing.getProduct().getId().equals(productId)) {
                int newQuantity = existing.getQuantity() + quantity;

                if (newQuantity > product.getStock()) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Insufficient stock for requested quantity");
                }

                existing.setQuantity(newQuantity);
                cart.setUpdatedAt(java.time.LocalDateTime.now());
                cartRepository.save(cart);
                return cartItemRepository.save(existing);
            }
        }

        cart.setUpdatedAt(java.time.LocalDateTime.now());
        cartRepository.save(cart);

        return cartItemRepository.save(
                new CartItem(cart, product, quantity));
    }

    @Transactional(readOnly = true)
    public List<CartItem> getItems(String email) {
        User user = getUser(email);

        return cartRepository.findByUser_Id(user.getId())
                .map(cart ->
                        cartItemRepository.findAllByCart_Id(cart.getId()))
                .orElse(List.of());
    }

    @Transactional
    public void updateQuantity(
            String email, Long itemId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero");
        }

        User user = getUser(email);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"));

        if (quantity > item.getProduct().getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Insufficient stock");
        }

        item.setQuantity(quantity);
        cart.setUpdatedAt(java.time.LocalDateTime.now());
        cartRepository.save(cart);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(String email, Long itemId) {
        User user = getUser(email);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository
                .findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"));

        cartItemRepository.delete(item);
    }
}
