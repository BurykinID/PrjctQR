package com.qr.app.backend.Json.container.dao;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class DescriptionContainerDao {

    private int numberLine;
    private String numberVariantBox;
    private int count;
    private String numberVariant;

}
