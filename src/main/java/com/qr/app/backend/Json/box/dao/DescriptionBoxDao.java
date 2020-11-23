package com.qr.app.backend.Json.box.dao;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class DescriptionBoxDao {

    private String numberVariant;
    private int numberLine;
    private String barcode;
    private int count;

}
