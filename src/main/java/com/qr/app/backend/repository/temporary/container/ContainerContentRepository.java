package com.qr.app.backend.repository.temporary.container;

import com.qr.app.backend.entity.forSession.temporarytable.container.ContainerContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface ContainerContentRepository extends JpaRepository<ContainerContent, Long> {

    Optional<ContainerContent> findByNumberVariantBox(String numberVariantBox);

    @Transactional
    @Modifying
    @Query("delete from ContainerContent cont where cont.macAddress = :macAddress")
    void deleteByMacAddress (@Param("macAddress") String macAddress);

    @Query("select cont from ContainerContent cont where cont.macAddress = :macAddress")
    List<ContainerContent> findByMacAddress(@Param("macAddress") String macAddress);

    List<ContainerContent> findByNumberContainer(String numberContainer);

}
