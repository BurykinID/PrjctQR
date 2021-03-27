package com.qr.app.backend.service;

import com.qr.app.backend.entity.order.container.Container;
import com.qr.app.backend.repository.order.container.ContainerRepository;
import com.qr.app.backend.status.ContainersStatusInDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContainerService {

    @Autowired
    private ContainerRepository containerRepository;

    public ContainersStatusInDb getStatusContainer(String numberContainer) {
        Container container = containerRepository.findByNumberContainer(numberContainer).orElse(new Container());
        if (container.getNumberContainer().isEmpty()) {
            return ContainersStatusInDb.NotFound;
        }
        else {
            switch (container.getStatus()) {
                case "" : return ContainersStatusInDb.Empty;
                case "В сборке" : return ContainersStatusInDb.InAssembly;
                case "Собран" : return ContainersStatusInDb.Assembled;
            }
        }
        return ContainersStatusInDb.NotFound;
    }

}
