package com.qr.app.backend.entity.order;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ord")
@Getter
@Setter
@Data
@NoArgsConstructor
public class Order {

    @Id
    @Column(unique = true)
    private int number;
    private long date;
    private String status;

    @OneToMany(mappedBy = "order")
    private List<VariantBox> variantBoxes;

}
