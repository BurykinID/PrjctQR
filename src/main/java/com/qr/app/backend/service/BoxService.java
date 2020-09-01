package com.qr.app.backend.service;

import com.qr.app.backend.entity.box.Box;
import com.qr.app.backend.repository.BoxRepository;
import org.springframework.stereotype.Service;

@Service
public class BoxService {

    private final BoxRepository boxRepository;

    public BoxService (BoxRepository boxRepository) {
        this.boxRepository = boxRepository;
    }

    public Box findBoxByNumberBox(String numberBox) {

        if (!numberBox.isEmpty()) {
            return boxRepository.findByNumberBox(numberBox);
        }
        else
            return new Box();

    }

}
