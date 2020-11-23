package com.qr.app.backend.repository.order.container;

import com.qr.app.backend.entity.order.container.VariantContainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VariantsContainerRepository extends JpaRepository<VariantContainer, Long> {

    Optional<VariantContainer> findByNumberVariant(String numberVariant);

}
