package com.qr.app.backend.repository.temporary;

import com.qr.app.backend.entity.forSession.BoxMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface BoxMarkReposiotry extends JpaRepository<BoxMark, Long> {

    List<BoxMark> findByNumberBox(String numberBox);

    Optional<BoxMark> findByNumberBoxAndCis(String numberBox, String cis);

    List<BoxMark> findByMacAddressAndNumberBox(String macAddress, String numberBox);

    List<BoxMark> findByMacAddress(String macAddress);

    @Transactional
    @Modifying
    @Query("delete from BoxMark bm where bm.macAddress = :macAddress")
    void deleteByMacAddress (@Param("macAddress") String macAddress);
}
