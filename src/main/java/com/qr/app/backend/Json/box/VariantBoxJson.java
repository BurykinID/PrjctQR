package com.qr.app.backend.Json.box;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class VariantBoxJson {

    private String numberVariant;
    private String numberOrder;
    private int countInBox;
    private int countBox;
    private String dateOrder;

}
