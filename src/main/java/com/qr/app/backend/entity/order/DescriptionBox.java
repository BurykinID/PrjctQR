package com.qr.app.backend.entity.order;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;
import javax.persistence.*;

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

}
