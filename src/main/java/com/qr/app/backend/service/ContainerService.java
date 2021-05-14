package com.qr.app.backend.service;

import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.entity.order.container.VariantContainer;
import com.qr.app.backend.repository.order.container.ContainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ContainerService {

    private final ContainerRepository containerRepository;

    public ContainerService(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    public Container findByNumberContainer (String numberContainer) {
        return containerRepository.findByNumberContainer(numberContainer).orElse(new Container());
    }

    public Container findByStatus(String status) {
        return containerRepository.findByStatus(status).orElse(new Container());
    }

    public void deleteByVariantContainer (VariantContainer variantContainer) {
        containerRepository.deleteByVariantContainer(variantContainer);
    }




}
