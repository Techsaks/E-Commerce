
package com.shop.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByCustomerEmailOrderByCreatedAtDesc(
            String customerEmail);
    
    java.util.Optional<Order> findByIdAndCustomerEmail(
            Long id, String customerEmail);
}
