package com.qr.app.backend.repository.order.box;

import com.qr.app.backend.entity.order.box.DescriptionBox;
import com.qr.app.backend.entity.order.box.VariantBox;
import com.qr.app.backend.entity.order.container.DescriptionContainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DescriptionBoxRepository extends JpaRepository<DescriptionBox, Long> {

    List<DescriptionBox> findByVariantBox(VariantBox variantBox);
}
