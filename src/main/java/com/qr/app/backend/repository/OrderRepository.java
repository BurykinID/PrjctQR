package com.qr.app.backend.repository;

import com.qr.app.backend.entity.box.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
