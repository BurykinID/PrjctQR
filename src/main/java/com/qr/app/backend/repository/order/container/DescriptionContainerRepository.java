package com.qr.app.backend.repository.order.container;

import com.qr.app.backend.entity.order.box.VariantBox;
import com.qr.app.backend.entity.order.container.DescriptionContainer;
import com.qr.app.backend.entity.order.container.VariantContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface DescriptionContainerRepository extends JpaRepository<DescriptionContainer, Long> {

    List<DescriptionContainer> findByVariantContainer(VariantContainer variantContainer);

    Optional<DescriptionContainer> findByNumberVariantBoxAndVariantContainer (String numberVariantBox, VariantContainer variantContainer);

    @Transactional
    @Modifying
    @Query("delete from DescriptionContainer descriptionContainer where descriptionContainer.variantContainer = :numberVariant")
    void deleteByNumberVariant(@Param("numberVariant") VariantContainer numberVariant);

}
