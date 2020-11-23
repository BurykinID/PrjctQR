package com.qr.app.backend.entity.order.container;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
@Data
public class DescriptionContainer extends AbstractEntity {

    private int numberLine;
    private String numberVariantBox;
    private int count;

    @ManyToOne
    @JoinColumn (name = "number_variant")
    private VariantContainer variantContainer;

    public DescriptionContainer() {
        this.numberLine = 0;
        this.numberVariantBox = "";
        this.count = 0;
        this.variantContainer = new VariantContainer();
    }

}
