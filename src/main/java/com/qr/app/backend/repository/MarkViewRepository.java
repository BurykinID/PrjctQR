package com.qr.app.backend.repository;

import com.qr.app.backend.entity.forSession.MarkView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface MarkViewRepository extends JpaRepository<MarkView, Long> {

    MarkView findByBarcode(String barcode);

    @Transactional
    @Modifying
    @Query("delete from MarkView mark where mark.macAddress = :macAddress")
    void deleteByMacAddress (@Param("macAddress") String macAddress);

    @Query("select mark from MarkView mark where mark.macAddress = :macAddress")
    List<MarkView> findByMacAddress(@Param("macAddress") String macAddress);

}
