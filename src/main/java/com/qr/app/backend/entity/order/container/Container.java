package com.qr.app.backend.entity.order.container;

import com.qr.app.backend.entity.AbstractEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class Container extends AbstractEntity {

    private String numberContainer;
    private String status;
    @ManyToOne
    @JoinColumn (name = "number_variant")
    private VariantContainer variantContainer;

}
