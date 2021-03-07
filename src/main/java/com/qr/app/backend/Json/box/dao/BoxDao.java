package com.qr.app.backend.Json.box.dao;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class BoxDao {

    private String numberVariant;
    private String numberBox;
    private String status;

    public BoxDao (String numberVariant, String numberBox, String status) {
        this.numberVariant = numberVariant;
        this.numberBox = numberBox;
        this.status = status;
    }
}
