package com.qr.app.backend.repository.order.box;

import com.qr.app.backend.entity.order.box.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
