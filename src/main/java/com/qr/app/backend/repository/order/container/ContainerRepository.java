package com.qr.app.backend.repository.order.container;

import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.entity.order.container.VariantContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Optional;

public interface ContainerRepository extends JpaRepository<Container, Long> {

    @Query("select cont from Container cont where cont.numberContainer = :numberContainer")
    Optional<Container> findByNumberContainer(@Param("numberContainer") String numberContainer);

    @Query("select cont from Container cont where cont.status = :status")
    Optional<Container> findByStatus(@Param("status") String status);

    @Transactional
    @Modifying
    @Query("delete from Container container where container.variantContainer = :variantContainer")
    void deleteByVariantContainer(@Param("variantContainer") VariantContainer variantContainer);

}
