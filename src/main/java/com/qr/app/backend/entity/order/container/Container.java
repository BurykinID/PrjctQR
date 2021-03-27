package com.qr.app.backend.entity.order.container;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.springframework.data.util.Lazy;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Container extends AbstractEntity{

    @Column(unique = true)
    private String numberContainer;
    private String status;
    @ManyToOne
    @JoinColumn (name = "number_variant")
    private VariantContainer variantContainer;

    public Container(String numberContainer, String status, VariantContainer variantContainer) {
        this.numberContainer = numberContainer;
        this.status = status;
        this.variantContainer = variantContainer;
    }

    public Container() {
        this.numberContainer = "";
        this.status = "";
        this.variantContainer = new VariantContainer();
    }

}
