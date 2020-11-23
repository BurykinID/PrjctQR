package com.qr.app.backend.repository.order.container;

import com.qr.app.backend.entity.order.box.VariantBox;
import com.qr.app.backend.entity.order.container.DescriptionContainer;
import com.qr.app.backend.entity.order.container.VariantContainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DescriptionContainerRepository extends JpaRepository<DescriptionContainer, Long> {

    List<DescriptionContainer> findByVariantContainer(VariantContainer variantContainer);

    Optional<DescriptionContainer> findByNumberVariantBoxAndVariantContainer (String numberVariantBox, VariantContainer variantContainer);
}
