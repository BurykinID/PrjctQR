package com.qr.app.backend.service;

import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.repository.order.container.ContainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContainerService {

    @Autowired
    private static ContainerRepository containerRepository;

    public static Container getContainerByMac(String macAddress) {

        Container

    }

}
