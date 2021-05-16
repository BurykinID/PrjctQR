package com.qr.app.backend.Json;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@NoArgsConstructor
public class BoxOperator {

    private String name;
    private String count;
    private String macAddress;

}
