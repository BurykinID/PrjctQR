package com.qr.app.backend.entity.box;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.*;
import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class DescriptionBox extends AbstractEntity {

    // numberVariant лишний
    private String numberVariant;
    private int numberLine;
    private String barcode;
    private int count;

    @ManyToOne
    @JoinColumn(name = "number_variant_id")
    private VariantBox variantBox;

}
