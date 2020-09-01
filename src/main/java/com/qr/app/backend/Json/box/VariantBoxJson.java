package com.qr.app.backend.Json.box;

import com.qr.app.backend.entity.box.DescriptionBox;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

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
