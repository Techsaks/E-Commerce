
package com.shop.payment;

import com.shop.order.Order;
import com.shop.order.OrderRepository;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponse processPayment(
            Long orderId, String email, @NotNull boolean simulateSuccess) {

        Order order = orderRepository
                .findByIdAndCustomerEmail(orderId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending orders can be paid");
        }

        if (paymentRepository.findByOrder_Id(orderId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A payment attempt already exists for this order");
        }

        String status = simulateSuccess ? "SUCCESS" : "FAILED";

        Payment payment = new Payment(
                order,
                order.getTotal(),
                status,
                "SIM-" + UUID.randomUUID());

        Payment savedPayment = paymentRepository.save(payment);

        if (simulateSuccess) {
            order.setStatus("PAID");
            orderRepository.save(order);
        }

        return PaymentResponse.from(savedPayment);
    }

}
