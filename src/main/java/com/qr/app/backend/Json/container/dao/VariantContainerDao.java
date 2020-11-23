package com.qr.app.backend.Json.container.dao;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class VariantContainerDao {

    private String numberVariant;
    private int countInBox;
    private int countBox;

}
