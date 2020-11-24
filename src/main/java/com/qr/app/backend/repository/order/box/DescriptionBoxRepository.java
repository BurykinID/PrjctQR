package com.qr.app.backend.repository.order.box;

import com.qr.app.backend.entity.order.box.DescriptionBox;
import com.qr.app.backend.entity.order.box.VariantBox;
import com.qr.app.backend.entity.order.container.DescriptionContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sun.security.krb5.internal.crypto.Des;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface DescriptionBoxRepository extends JpaRepository<DescriptionBox, Long> {

    List<DescriptionBox> findByVariantBox(VariantBox variantBox);

    @Transactional
    @Modifying
    @Query ("delete from DescriptionBox descriptionBox where descriptionBox.variantBox = :variantBox")
    void deleteByNumberVariant (@Param ("variantBox") VariantBox variantBox);

}
