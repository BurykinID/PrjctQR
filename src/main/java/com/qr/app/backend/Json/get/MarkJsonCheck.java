package com.qr.app.backend.Json.get;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class MarkJsonCheck {

    private String cis;
    private String numberBox;
    private String barcode;

    public MarkJsonCheck(String cis, String numberBox, String date) {
        this.cis = cis;
        this.numberBox = numberBox;
        this.barcode = date;
    }

}