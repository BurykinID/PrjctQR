package com.qr.app.backend.service;

import com.qr.app.backend.repository.VariantsBoxRepostiorty;
import org.springframework.stereotype.Service;

@Service
public class VariantsBoxServise {

    private final VariantsBoxRepostiorty variantsBoxRepostiorty;

    public VariantsBoxServise (VariantsBoxRepostiorty variantsBoxRepostiorty) {
        this.variantsBoxRepostiorty = variantsBoxRepostiorty;
    }

}
