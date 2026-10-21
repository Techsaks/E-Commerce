
package com.shop.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerEmail,
        BigDecimal total,
        String status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items) {

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> itemResponses =
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity()))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses);
    }

    public record OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            BigDecimal unitPrice,
            int quantity) {
    }
}
