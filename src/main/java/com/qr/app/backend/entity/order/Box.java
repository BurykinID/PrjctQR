package com.qr.app.backend.entity.order;

import com.qr.app.backend.entity.AbstractEntity;
import com.sun.istack.Nullable;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class Box extends AbstractEntity {

    private String numberBox;
    private String status;
    @ManyToOne
    @JoinColumn(name = "number_variant")
    private VariantBox variantBox;

}
