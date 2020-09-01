package com.qr.app.backend.service;

import com.qr.app.backend.repository.GoodRepository;
import org.springframework.stereotype.Service;

@Service
public class GoodServise {

    private final GoodRepository itemRepository;

    public GoodServise (GoodRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


}
