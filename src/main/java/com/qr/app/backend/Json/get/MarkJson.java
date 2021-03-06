package com.qr.app.backend.Json.get;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class MarkJson {

    private String cis;
    private String numberBox;
    private String lastUpdate;

    public MarkJson(String cis, String numberBox, String date) {
        this.cis = cis;
        this.numberBox = numberBox;
        this.lastUpdate = date;
    }

}
