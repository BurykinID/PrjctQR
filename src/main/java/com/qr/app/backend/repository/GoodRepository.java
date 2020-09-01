package com.qr.app.backend.repository;

import com.qr.app.backend.entity.Good;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodRepository extends JpaRepository<Good, Long> {

    Good findByBarcode(String barcode);

}
