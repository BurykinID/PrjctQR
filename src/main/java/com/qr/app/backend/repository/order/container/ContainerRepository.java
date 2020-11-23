package com.qr.app.backend.repository.order.container;

import com.qr.app.backend.entity.order.container.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContainerRepository extends JpaRepository<Container, Long> {

    @Query("select cont from Container cont where cont.numberContainer = :numberContainer")
    Optional<Container> findByNumberContainer(@Param("numberContainer") String numberContainer);

    @Query("select cont from Container cont where cont.status = :status")
    Optional<Container> findByStatus(@Param("status") String status);

}
