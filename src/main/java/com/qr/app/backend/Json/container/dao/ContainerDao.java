package com.qr.app.backend.Json.container.dao;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class ContainerDao {

    private String numberVariant;
    private String numberContainer;
    private String status;

}
