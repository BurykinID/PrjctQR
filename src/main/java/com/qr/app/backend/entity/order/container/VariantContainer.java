package com.qr.app.backend.entity.order.container;

import com.qr.app.backend.entity.order.box.Order;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@Data
public class VariantContainer {

    @Id
    @Column (unique = true)
    private String numberVariant;
    private int countInBox;
    private int countBox;

    @OneToMany (mappedBy = "variantContainer")
    private List<DescriptionContainer> descriptionContainers;

    @OneToMany(mappedBy = "variantContainer")
    private List<Container> containers;

    @ManyToOne
    @JoinColumn(name = "order_container")
    private OrderContainer orderContainer;

    public VariantContainer() {
        this.numberVariant = "";
        this.countBox = 0;
        this.countInBox = 0;
        this.descriptionContainers = new LinkedList<>();
        this.containers = new LinkedList<>();
        this.orderContainer = new OrderContainer();
    }

}
