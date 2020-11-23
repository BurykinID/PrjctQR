package com.qr.app.backend.repository.temporary.box;

import com.qr.app.backend.entity.forSession.temporarytable.box.BoxContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface BoxContentRepository extends JpaRepository<BoxContent, Long> {

    Optional<BoxContent> findByBarcode(String barcode);

    @Transactional
    @Modifying
    @Query("delete from BoxContent mark where mark.macAddress = :macAddress")
    void deleteByMacAddress (@Param("macAddress") String macAddress);

    @Query("select mark from BoxContent mark where mark.macAddress = :macAddress")
    List<BoxContent> findByMacAddress(@Param("macAddress") String macAddress);

    List<BoxContent> findByNumberBox(String numberBox);

}
