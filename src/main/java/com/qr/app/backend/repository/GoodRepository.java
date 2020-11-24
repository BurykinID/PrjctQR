package com.qr.app.backend.repository;

import com.qr.app.backend.entity.Good;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoodRepository extends JpaRepository<Good, Long> {

    Optional<Good> findByBarcode(String barcode);

}
