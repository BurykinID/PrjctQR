package com.qr.app.backend.entity.order.container;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table (name = "ord_pack")
@Getter
@Setter
@Data

public class OrderContainer {

    @Id
    @Column (unique = true)
    private String number;
    private long date;
    private String status;

    @OneToMany (mappedBy = "orderContainer")
    private List<VariantContainer> variantContainers;

    public OrderContainer() {
        this.number = null;
        this.date = 0;
        this.status = "";
        this.variantContainers = new LinkedList<>();
    }

}
