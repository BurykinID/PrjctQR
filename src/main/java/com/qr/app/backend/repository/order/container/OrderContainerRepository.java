package com.qr.app.backend.repository.order.container;

import com.qr.app.backend.entity.order.container.OrderContainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderContainerRepository extends JpaRepository<OrderContainer, Long> {

    Optional<OrderContainer> findByNumber(String number);

}
