package com.qr.app.backend.Json.box.dao;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class VariantBoxDao {

    private String numberVariant;
    private int countInBox;
    private int countBox;

    public VariantBoxDao (String numberVariant, int countInBox, int countBox) {
        this.numberVariant = numberVariant;
        this.countInBox = countInBox;
        this.countBox = countBox;
    }
}
