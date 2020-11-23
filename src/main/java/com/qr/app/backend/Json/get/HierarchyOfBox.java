package com.qr.app.backend.Json.get;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class HierarchyOfBox {

    private String numberBox;
    private String numberContainer;
    private String lastUpdate;

    public HierarchyOfBox (String numberBox, String numberContainer, String lastUpdate) {
        this.numberBox = numberBox;
        this.numberContainer = numberContainer;
        this.lastUpdate = lastUpdate;
    }

}
