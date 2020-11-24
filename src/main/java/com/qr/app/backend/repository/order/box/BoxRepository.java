package com.qr.app.backend.repository.order.box;

import com.qr.app.backend.entity.order.box.Box;
import com.qr.app.backend.entity.order.box.VariantBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Optional;

public interface BoxRepository extends JpaRepository<Box, Long> {

    @Query("select box from Box box where box.numberBox = :numberBox")
    Optional<Box> findByNumberBox(@Param("numberBox") String numberBox);

    @Query("select box from Box box where box.status = :status")
    Optional<Box> findByStatus(@Param("status") String status);

    @Transactional
    @Modifying
    @Query ("delete from Box b where b.variantBox = :variantBox")
    void deleteByVariantBox (@Param("variantBox") VariantBox variantBox);

}
