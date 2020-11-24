package com.qr.app.backend.entity.order.box;

import com.qr.app.backend.Json.box.OrderDao;
import com.qr.app.backend.Json.box.dao.DescriptionBoxDao;
import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;
import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class DescriptionBox extends AbstractEntity {

    private int numberLine;
    private String barcode;
    private int count;

    @ManyToOne
    @JoinColumn(name = "number_variant")
    private VariantBox variantBox;

    public DescriptionBox(String barcode, int numberLine, int count, VariantBox variantBox) {
        this.numberLine = numberLine;
        this.barcode = barcode;
        this.count = count;
        this.variantBox = variantBox;
    }

}
