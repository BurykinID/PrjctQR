package com.qr.app.backend.repository.order.box;

import com.qr.app.backend.entity.order.box.VariantBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface VariantsBoxRepository extends JpaRepository<VariantBox, Long> {

    @Query("select var from VariantBox var where var.numberVariant = :number_variant")
    Optional<VariantBox> findByNumberVariant(@Param("number_variant") String numberVariant);

    @Query("select variantBox from VariantBox variantBox where variantBox.order.number = :order_number")
    List<VariantBox> findByOrderNumber(@Param("order_number") String orderNumber);

    @Transactional
    @Modifying
    @Query ("delete from VariantBox variantBox where variantBox.order.number = :order_number")
    void deleteByOrderNumber (@Param("order_number") String orderNumber);

}
