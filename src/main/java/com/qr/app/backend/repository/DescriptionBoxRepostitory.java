package com.qr.app.backend.repository;

import com.qr.app.backend.entity.box.DescriptionBox;
import com.qr.app.backend.entity.box.VariantBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DescriptionBoxRepostitory extends JpaRepository<DescriptionBox, Long> {

    List<DescriptionBox> findByVariantBox(VariantBox variantBox);

}
