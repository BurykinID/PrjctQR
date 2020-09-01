package com.qr.app.backend.service;

import com.qr.app.backend.repository.DescriptionBoxRepostitory;
import org.springframework.stereotype.Service;

@Service
public class DescriptionBoxService {

    private final DescriptionBoxRepostitory descriptionBoxRepostitory;

    public DescriptionBoxService (DescriptionBoxRepostitory descriptionBoxRepostitory) {
        this.descriptionBoxRepostitory = descriptionBoxRepostitory;
    }
}
