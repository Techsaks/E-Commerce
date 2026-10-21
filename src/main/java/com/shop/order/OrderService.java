
package com.shop.order;

import com.shop.cart.Cart;
import com.shop.cart.CartItem;
import com.shop.cart.CartItemRepository;
import com.shop.cart.CartRepository;
import com.shop.product.Product;
import com.shop.product.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse checkout(String email) {

        Cart cart = cartRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cart not found"));

        List<CartItem> cartItems =
                cartItemRepository.findAllByCart_Id(cart.getId());

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;

        // Validate every item before changing inventory.
        for (CartItem cartItem : cartItems) {
            Product product = productRepository
                    .findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

            if (!product.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        product.getName() + " is no longer available");
            }

            if (cartItem.getQuantity() <= 0
                    || cartItem.getQuantity() > product.getStock()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for " + product.getName());
            }

            total = total.add(product.getPrice().multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())));
        }

        Order order = new Order(email, total);

        for (CartItem cartItem : cartItems) {
            Product product = productRepository
                    .findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

            product.setStock(
                    product.getStock() - cartItem.getQuantity());

            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    cartItem.getQuantity());

            order.addItem(orderItem);
        }

        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAll(new ArrayList<>(cartItems));
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {
        return orderRepository
                .findAllByCustomerEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }
}
