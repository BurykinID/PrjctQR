package com.qr.app.backend.repository.order;

import com.qr.app.backend.entity.order.DescriptionBox;
import com.qr.app.backend.entity.order.VariantBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DescriptionBoxRepostitory extends JpaRepository<DescriptionBox, Long> {

    List<DescriptionBox> findByVariantBox(VariantBox variantBox);

}
