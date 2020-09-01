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
public class Box extends AbstractEntity {

    // лишняя строка
    private String numberVariant;
    private String numberBox;
    private String status;

    @ManyToOne
    @JoinColumn(name = "number_variant_id")
    private VariantBox variantBox;

}
