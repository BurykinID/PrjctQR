package com.qr.app.backend.entity.order.box;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class Box extends AbstractEntity {

    @Column(unique = true)
    private String numberBox;
    private String status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "number_variant")
    private VariantBox variantBox;

    public Box(String numberBox, String status, VariantBox variantBox) {
        this.numberBox = numberBox;
        this.status = status;
        this.variantBox = variantBox;
    }

}
