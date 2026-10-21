package com.shop.order;

import com.shop.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByUserOrderByCreatedAtDesc(User user);
}