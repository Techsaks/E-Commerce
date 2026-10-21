
package com.shop.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(Authentication authentication) {
        return orderService.checkout(authentication.getName());
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(
            Authentication authentication) {
        return orderService.getMyOrders(authentication.getName());
    }
}
