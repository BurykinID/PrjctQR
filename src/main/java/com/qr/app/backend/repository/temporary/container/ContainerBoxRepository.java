package com.qr.app.backend.repository.temporary.container;

import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface ContainerBoxRepository extends JpaRepository<ContainerBox, Long> {

    List<ContainerBox> findByNumberContainer(String numberContainer);

    Optional<ContainerBox> findByNumberContainerAndNumberBox(String numberContainer, String numberBox);

    List<ContainerBox> findByMacAddressAndNumberContainer(String macAddress, String numberContainer);

    List<ContainerBox> findByMacAddress(String macAddress);

    @Transactional
    @Modifying
    @Query("delete from ContainerBox contBox where contBox.macAddress = :macAddress")
    void deleteByMacAddress (@Param("macAddress") String macAddress);
    Optional<ContainerBox> findByNumberBox(String numberBox);

}
