package com.qr.app.backend.entity.order.box;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class VariantBox {

    @Id
    @Column(unique = true)
    private String numberVariant;
    private int countInBox;
    private int countBox;

    @OneToMany(mappedBy = "variantBox")
    private List<DescriptionBox> descriptionBoxes;

    @OneToMany(mappedBy = "variantBox")
    private List<Box> boxes;

    @ManyToOne
    @JoinColumn(name = "order_number")
    private Order order;

}
